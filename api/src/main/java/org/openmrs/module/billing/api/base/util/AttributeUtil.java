/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base.util;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.Attributable;
import org.openmrs.customdatatype.NotYetPersistedException;
import org.openmrs.module.billing.api.base.Utility;
import org.openmrs.util.OpenmrsClassLoader;

/**
 * Utility class for working with {@link org.openmrs.attribute.Attribute}'s.
 */
@Slf4j
public class AttributeUtil {
	
	private AttributeUtil() {
	}
	
	/**
	 * Attempts to create a new instance of the specified class and hydrate (deserialize) it using the
	 * specified string value.
	 *
	 * @param className The class name for the expected instance
	 * @param value The serialized object data
	 * @return A new hydrated instance or {@code null} if the instance could not be loaded.
	 */
	@SuppressWarnings("unchecked")
	public static Object tryToHydrateObject(String className, String value) {
		// TODO: Refactor this.
		//  This method assumes a lot about what kind of class is being used to store the serialized data
		//  (Attributable). If we assume that the data is in an Attributable than this method can be simplified.  If
		//  not, it should use the general java serialization stuff unless the class is some type we know about and can
		//  do some kind of special deserialization for.
		
		Object result = value;
		
		try {
			Class cls = Class.forName(className);
			if (Attributable.class.isAssignableFrom(cls)) {
				try {
					Class c = OpenmrsClassLoader.getInstance().loadClass(className);
					
					// Attempt to hydrate the attribute using Attributable.hydrate(String)
					try {
						Object instance = c.newInstance();
						
						Attributable attr = Utility.as(Attributable.class, instance);
						if (attr != null) {
							result = attr.hydrate(value);
						}
					}
					catch (InstantiationException e) {
						// try to hydrate the object with the String constructor
						log.trace("Unable to call no-arg constructor for class: {}", c.getName());
						
						result = c.getConstructor(String.class).newInstance(value);
					}
				}
				catch (NotYetPersistedException e) {
					result = null;
				}
				catch (Exception ex) {
					log.warn("Unable to hydrate value: {} for type: {}", value, className, ex);
				}
			}
		}
		catch (ClassNotFoundException cnfe) {
			log.warn("Unable to parse '{}' to a known class.", className);
		}
		
		return result;
	}
}
