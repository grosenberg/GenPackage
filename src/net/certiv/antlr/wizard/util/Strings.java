/*******************************************************************************
 * Copyright (c) 2008-2014 G Rosenberg.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		G Rosenberg - initial API and implementation
 *
 * Versions:
 * 		1.0 - 2014.03.26: First release level code
 * 		1.1 - 2014.08.26: Updates, add Tests support
 *******************************************************************************/
package net.certiv.antlr.wizard.util;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

public class Strings {

	/** Platform dependent end-of-line marker */
	public static final String eol = System.lineSeparator();
	/** Platform dependent path separator mark */
	public static final char pathSep = File.separatorChar;
	/** classpath (and unix) separator) */
	public static final String STD_SEPARATOR = "/";
	// Windows separator character.
	private static final String WINDOWS_SEPARATOR = "\\\\";

	public static String camelCase(String in) {
		StringBuilder sb = new StringBuilder(in);
		for (int idx = sb.length() - 1; idx >= 0; idx--) {
			char c = sb.charAt(idx);
			if (c == '_') {
				sb.deleteCharAt(idx);
				sb.setCharAt(idx, Character.toUpperCase(sb.charAt(idx)));
			} else if (Character.isUpperCase(c)) {
				sb.setCharAt(idx, Character.toLowerCase(c));
			}
		}
		sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
		return sb.toString();
	}

	public static String tokenCase(String in) {
		if (isAllUpperCase(in)) {
			return Character.toString(in.charAt(0)) + in.substring(1).toLowerCase();
		}
		return in;
	}

	public static boolean isAllUpperCase(String in) {
		for (int idx = 0; idx < in.length(); idx++) {
			if (Character.isLowerCase(in.charAt(idx))) {
				return false;
			}
		}
		return true;
	}

	public static String titleCase(String word) {
		if (word == null || word.length() == 0) return "";
		if (word.length() == 1) return word.toUpperCase();
		return word.substring(0, 1).toUpperCase() +
				word.substring(1).toLowerCase();
	}

	public static String concat(String... args) {
		String result = "";
		for (String arg : args) {
			result = FilenameUtils.concat(result, arg);
		}
		return result;
	}

	/*
	 * Convert separators so the string is a valid URL appropriate for classpath discovery
	 */
	public static String concatAsClassPath(String... args) {
		return concat(args).replaceAll(WINDOWS_SEPARATOR, STD_SEPARATOR);
	}
}
