/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.billing.api.base;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 * General utility methods.
 */
@Slf4j
public class Utility {
	
	private static final int DATE_ONLY_TEXT_LENGTH = 10;
	
	private static final int DATE_TIME_TEXT_LENGTH = 16;
	
	private static final int DATE_TIME_SECOND_TEXT_LENGTH = 19;
	
	private Utility() {
	}
	
	/**
	 * Returns the specified object as the specified class or returns null if the cast is not supported.
	 *
	 * @param cls The generic class to cast the object to.
	 * @param o The object to cast.
	 * @param <T> The generic class to cast the object to.
	 * @return The object cast to the specified class or {@code null} if the cast is not supported.
	 */
	public static <T> T as(Class<T> cls, Object o) {
		if (cls.isInstance(o)) {
			return cls.cast(o);
		}
		return null;
	}
	
	/**
	 * Clears the time portion of the specified {@link Calendar}, setting the hour, minute, second, and
	 * millisecond parts to 0.
	 *
	 * @param cal The calendar object to clear the time portion from.
	 */
	public static void clearCalendarTime(Calendar cal) {
		if (cal == null) {
			throw new IllegalArgumentException("The calendar must be defined.");
		}
		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
	}
	
	/**
	 * Parses a standard OpenHMIS formatted (openhmis.dateFormat) date returning the
	 * {@link java.util.Date} object.
	 *
	 * @param dateText The date text to parse
	 * @return The date or null if the text cannot be parsed.
	 */
	public static Date parseOpenhmisDateString(String dateText) {
		if (StringUtils.isEmpty(dateText)) {
			return null;
		}
		
		SimpleDateFormat dateFormat = null;
		if (dateText.length() == DATE_ONLY_TEXT_LENGTH) {
			dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		} else if (dateText.length() == DATE_TIME_TEXT_LENGTH) {
			dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		} else if (dateText.length() == DATE_TIME_SECOND_TEXT_LENGTH) {
			dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		}
		
		Date result = null;
		if (dateFormat == null) {
			log.warn("Could not parse the date string '{}'.", dateText);
		} else {
			try {
				result = dateFormat.parse(dateText);
			}
			catch (ParseException pex) {
				log.warn("Could not parse the date string '{}'.", dateText, pex);
			}
		}
		
		return result;
	}
}
