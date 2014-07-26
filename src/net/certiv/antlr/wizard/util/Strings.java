package net.certiv.antlr.wizard.util;

import java.io.File;

public class Strings {

	/** Platform dependent end-of-line marker */
	public static final String eol = System.lineSeparator();
	/** Platform dependent path separator mark */
	public static final char pathSep = File.separatorChar;
	/** classpath (and unix) separator) */
	public static final String STD_SEPARATOR = "/";

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
}
