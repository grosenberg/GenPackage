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
package net.certiv.antlr.project.util;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

public class Strings {

	/** Platform dependent end-of-line marker */
	public static final String eol = System.lineSeparator();
	/** Platform dependent path separator mark as char */
	public static final char pathSep = File.separatorChar;
	/** Platform dependent path separator mark as string */
	public static final String pathSepStr = File.separator;
	/** Platform dependent path separator mark */
	public static final String pkgSep = ".";
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

	/**
	 * Convert a list of strings to a standard csv representation
	 */
	public static String toCsv(List<String> strs) {
		StringBuilder sb = new StringBuilder();
		for (String s : strs) {
			sb.append(s + ", ");
		}
		if (sb.length() > 1) sb.setLength(sb.length() - 2);
		return sb.toString();
	}

	public static String trimLead(String str) {
		return str.replaceAll("^\\s+", "");
	}

	public static String trimTrail(String str) {
		return str.replaceAll("\\s+$", "");
	}

	/**
	 * Concats the arguments to produce a filesystem path fragment. Separators are added as needed.
	 * All separators are converted to standard separators, ie, *nix-style.
	 */
	public static String concat(String... args) {
		String result = "";
		for (String arg : args) {
			result = FilenameUtils.concat(result, arg);
		}
		return result;
	}

	/**
	 * Concats the arguments to produce a valid string URL appropriate for classpath discovery.
	 * Separators are added as needed. All separators are converted to standard separators, ie,
	 * *nix-style.
	 */
	public static String concatAsClassPath(String... args) {
		return concat(args).replaceAll(WINDOWS_SEPARATOR, STD_SEPARATOR);
	}

	/**
	 * Converts a standard java package-styled string to a corresponding filesystem path fragment.
	 * */
	public static String convertPkgToPath(String arg) {
		return arg.replace(pkgSep, STD_SEPARATOR);
	}

	/**
	 * Removes any existing extension appended to the name, and then appends the given extension.
	 * Checks to ensure that a dot separator is present between the name and extension.
	 */
	public static String setExtension(String name, String ext) {
		int dot = name.lastIndexOf('.');
		if (dot != -1) {
			name = name.substring(0, dot);
		}
		if (!ext.startsWith(".")) {
			ext = "." + ext;
		}
		return name + ext;
	}

	public static String initialLC(String str) {
		if (str == null) return "";
		if (str.length() < 2) return str.toLowerCase();
		return str.substring(0, 1).toLowerCase() + str.substring(1);
	}

	public static String initialUC(String str) {
		if (str == null) return "";
		if (str.length() < 2) return str.toUpperCase();
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}
