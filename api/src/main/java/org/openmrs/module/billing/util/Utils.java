/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.billing.util;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationAttribute;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;

public class Utils {
	
	public static String UNIQUE_PATIENT_NUMBER = "05ee9cf4-7242-4a17-b4d4-00f707265c8a";
	
	public static final String HEI_UNIQUE_NUMBER = "0691f522-dd67-4eeb-92c8-af5083baf338";
	
	public static final String KDOD_NUMBER = "b51ffe55-3e76-44f8-89a2-14f5eaf11079";
	
	static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy");
	
	public static final String MCH_MOTHER_SERVICE_PROGRAM = "b5d9e05f-f5ab-4612-98dd-adb75438ed34";
	
	public static final String RECENCY_ID = "fd52829a-75d2-4732-8e43-4bff8e5b4f1a";
	
	public static SimpleDateFormat getSimpleDateFormat(String pattern) {
		return new SimpleDateFormat(pattern);
	}
	
	/**
	 * Creates a node factory
	 *
	 * @return
	 */
	public static JsonNodeFactory getJsonNodeFactory() {
		final JsonNodeFactory factory = JsonNodeFactory.instance;
		return factory;
	}
	
	/**
	 * Extracts the request body and return it as string
	 *
	 * @param reader
	 * @return
	 */
	public static String fetchRequestBody(BufferedReader reader) {
		String requestBodyJsonStr = "";
		try {
			
			BufferedReader br = new BufferedReader(reader);
			String output = "";
			while ((output = reader.readLine()) != null) {
				requestBodyJsonStr += output;
			}
		}
		catch (IOException e) {
			
		}
		return requestBodyJsonStr;
	}
	
	/**
	 * Finds the last encounter during the program enrollment with the given encounter type Picked for
	 * EmrUtils
	 *
	 * @param type the encounter type
	 * @return the encounter
	 */
	public static Encounter lastEncounter(Patient patient, EncounterType type) {
		List<Encounter> encounters = Context.getEncounterService().getEncounters(patient, null, null, null, null,
		    Collections.singleton(type), null, null, null, false);
		return encounters.size() > 0 ? encounters.get(encounters.size() - 1) : null;
	}
	
	/**
	 * Check latest obs values for given obs
	 *
	 * @param patient
	 * @return latest obs
	 */
	public static Obs getLatestObs(Patient patient, String conceptIdentifier) {
		Concept concept = Context.getConceptService().getConceptByUuid(conceptIdentifier);
		List<Obs> obs = Context.getObsService().getObservationsByPersonAndConcept(patient, concept);
		if (obs.size() > 0) {
			// these are in reverse chronological order
			return obs.get(0);
		}
		return null;
	}
	
	/**
	 * Get date difference between two dates (in days)
	 *
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int daysBetween(Date date1, Date date2) {
		DateTime d1 = new DateTime(date1.getTime());
		DateTime d2 = new DateTime(date2.getTime());
		return Days.daysBetween(d1, d2).getDays();
	}
	
	/**
	 * Gets the integer value of a string, otherwise returns zero
	 *
	 * @param val
	 * @return
	 */
	public static int getIntegerValue(String val) {
		int ret = 0;
		try {
			ret = (int) Math.ceil(Double.parseDouble(val));
		}
		catch (Exception ex) {}
		return (ret);
	}
	
	/**
	 * Gets the long value of a string, otherwise returns zero
	 *
	 * @param val
	 * @return
	 */
	public static long getLongValue(String val) {
		long ret = 0;
		try {
			ret = (long) Math.ceil(Double.parseDouble(val));
		}
		catch (Exception ex) {}
		return (ret);
	}
	
	/**
	 * Builds an SSL context for disabling/bypassing SSL verification
	 *
	 * @return
	 */
	public static SSLConnectionSocketFactory sslConnectionSocketFactoryWithDisabledSSLVerification() {
		SSLContextBuilder builder = SSLContexts.custom();
		try {
			builder.loadTrustMaterial(null, new TrustStrategy() {
				
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			});
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		catch (KeyStoreException e) {
			throw new RuntimeException(e);
		}
		SSLContext sslContext = null;
		try {
			sslContext = builder.build();
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		catch (KeyManagementException e) {
			throw new RuntimeException(e);
		}
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {
			
			@Override
			public void verify(String host, SSLSocket ssl) throws IOException {
			}
			
			@Override
			public void verify(String host, X509Certificate cert) throws SSLException {
			}
			
			@Override
			public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
			}
			
			@Override
			public boolean verify(String s, SSLSession sslSession) {
				return true;
			}
		});
		return sslsf;
	}
	
	/**
	 * Default SSL context
	 *
	 * @return
	 */
	public static SSLConnectionSocketFactory sslConnectionSocketFactoryDefault() {
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(SSLContexts.createDefault(),
		        new String[] { "TLSv1.2" }, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		return sslsf;
	}
	
}
