/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.internal.common.ui.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;

public class DateTimeUtils {

	private static long NANOSECONDS_PER_SEC = 1000000;

	private DateTimeUtils() {
	}

	public static String formatDuration(long nanoseconds) {
		String[] parts = DurationFormatUtils.formatDuration(nanoseconds / NANOSECONDS_PER_SEC, "H:m:s:S").split(":");
		StringBuilder builder = new StringBuilder();
		if (Integer.valueOf(parts[0]) > 0) {
			builder.append(parts[0]).append(" hrs.");
		}
		if (Integer.valueOf(parts[1]) > 0) {
			builder.append(" ").append(parts[1]).append(" min.");
		}
		if (Integer.valueOf(parts[2]) > 0) {
			builder.append(" ").append(parts[2]).append(" sec.");
		}
		if (builder.length() == 0) {
			builder.append("Now");
		}
		return builder.toString().trim();
	}

	public static String formatSince(String value) {
		return formatSince(value, null);
	}

	public static String formatSince(String value, TimeZone timezone) {
		DateTimeFormatter dtf = DateTimeFormatter.ISO_INSTANT;
		if (timezone != null) {
		    dtf = dtf.withZone(timezone.toZoneId());
		} else {
		    dtf = dtf.withZone(ZoneOffset.UTC);
		}
		ZonedDateTime zonedDateTime = ZonedDateTime.parse(value, dtf);
        return zonedDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yy h:mm:ss a VV"));
	}

	public static Date parse(String value) throws ParseException {
		if (StringUtils.isEmpty(value)) {
			//prevent null pointer and index out of bounds exceptions.
			throw new ParseException("Date is not provided", 0);
		}
		//ref: http://www.java2s.com/Code/Java/Data-Type/ISO8601dateparsingutility.htm
		//assume date is like: '2015-11-11T20:32:37Z' 
		String modValue = value.substring(0, value.length() - 1) + "GMT-00:00";
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
		return parser.parse(modValue);
	}
}
