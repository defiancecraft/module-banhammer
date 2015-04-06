package com.defiancecraft.modules.banhammer.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DurationUtils {

	private static final Pattern DURATION_PATTERN = Pattern.compile("\\s*(\\d+)\\s*(s|seconds|m|minutes|h|hours|d|days|w|weeks)");
	
	public static boolean isValidDuration(String duration) {
		return toSeconds(duration) > -1;
	}
	
	/**
	 * Converts a duration string of the form:
	 * x{s|m|h|d|w} to its value in seconds.
	 * 
	 * This must not exceed `Integer.MAX_VALUE`
	 * when converted to seconds, so checks are in
	 * place to prevent such from happening.
	 * 
	 * If the duration given is invalid, -1 is returned.
	 * 
	 * @param duration Duration to parse
	 * @return Given duration in seconds, or -1 if failed.
	 */
	public static int toSeconds(String duration) {
		
		Matcher m = DURATION_PATTERN.matcher(duration);
		if (!m.matches()) return -1;
		
		String lengthStr = m.group(1);
		String multiplierStr = m.group(2);
		
		if (lengthStr == null || lengthStr.isEmpty()
			|| multiplierStr == null || multiplierStr.isEmpty())
			return -1;
		
		int multiplier = 1;
		int length;
		
		try {
			length = Integer.parseInt(lengthStr); 
		} catch (Exception e) { return -1; }
		
		switch (multiplierStr.charAt(0)) {
		case 's': multiplier = 1; break;
		case 'm': multiplier = 60; break;
		case 'h': multiplier = 3600; break;
		case 'd': multiplier = 86400; break;
		case 'w': multiplier = 604800; break;
		}
		
		if ((float)length / multiplier >= (float)Integer.MAX_VALUE / multiplier)
			return -1;
		
		return length * multiplier;
		
	}
	
}
