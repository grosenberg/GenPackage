/*
 * [The "BSD license"]
 *  Copyright (c) 2012 Gerald Rosenberg, Certiv Analytics
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.certiv.antlr.wizard.util;

import org.apache.log4j.Logger;

public class Log {

	private static Logger logger = Logger.getLogger(Log.class);

	private Log() {
	}

	public static void info(Object source, String message) {
		info(source, true, message, null);
	}

	public static void info(Object source, boolean debug, String message) {
		info(source, debug, message, null);
	}

	public static void info(Object source, String message, Throwable e) {
		info(source, true, message, e);
	}

	public static void info(Object source, boolean debug, String message, Throwable e) {
		if (!debug) return;
		logger = Logger.getLogger(source.getClass());
		if (e == null) {
			logger.info(message);
		} else {
			logger.info(message, e);
		}
	}

	public static void debug(Object source, String message) {
		debug(source, true, message, null);
	}

	public static void debug(Object source, boolean debug, String message) {
		debug(source, debug, message, null);
	}

	public static void debug(Object source, String message, Throwable e) {
		debug(source, true, message, e);
	}

	public static void debug(Object source, boolean debug, String message, Throwable e) {
		if (!debug) return;
		logger = Logger.getLogger(source.getClass());
		if (e == null) {
			logger.debug(message);
		} else {
			logger.debug(message, e);
		}
	}

	public static void warn(Object source, String message) {
		warn(source, true, message, null);
	}

	public static void warn(Object source, boolean debug, String message) {
		warn(source, debug, message, null);
	}

	public static void warn(Object source, String message, Throwable e) {
		warn(source, true, message, e);
	}

	public static void warn(Object source, boolean debug, String message, Throwable e) {
		if (!debug) return;
		logger = Logger.getLogger(source.getClass());
		if (e == null) {
			logger.warn(message);
		} else {
			logger.warn(message, e);
		}
	}

	public static void error(Object source, String message) {
		error(source, true, message, null);
	}

	public static void error(Object source, boolean debug, String message) {
		error(source, debug, message, null);
	}

	public static void error(Object source, String message, Throwable e) {
		error(source, true, message, e);
	}

	public static void error(Object source, boolean debug, String message, Throwable e) {
		if (!debug) return;
		logger = Logger.getLogger(source.getClass());
		if (e == null) {
			logger.error(message);
		} else {
			logger.error(message, e);
		}
	}

	public static void fatal(Object source, String message) {
		fatal(source, true, message, null);
	}

	public static void fatal(Object source, boolean debug, String message) {
		fatal(source, debug, message, null);
	}

	public static void fatal(Object source, String message, Throwable e) {
		fatal(source, true, message, e);
	}

	public static void fatal(Object source, boolean debug, String message, Throwable e) {
		if (!debug) return;
		logger = Logger.getLogger(source.getClass());
		if (e == null) {
			logger.fatal(message);
		} else {
			logger.fatal(message, e);
		}
	}
}