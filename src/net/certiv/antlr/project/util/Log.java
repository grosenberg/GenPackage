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
 * 		1.1 - 2014.07.25: Updated to use Log4j2
 * 		1.2 - 2014.11.01: Fixed default logger bug
 *******************************************************************************/
package net.certiv.antlr.project.util;

import java.util.HashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;
import org.apache.logging.log4j.spi.LoggerContext;

public class Log extends ExtendedLoggerWrapper {

	private static final long serialVersionUID = 1L;

	private static Logger logger;
	private static final String FQCN = Log.class.getName();

	private static HashMap<Integer, LogLevel> logLevels = new HashMap<>();
	private static int LogId = Log.class.hashCode();

	// set the global default log value
	static {
		setLevel(LogId, LogLevel.Warn);
	}

	public Log(ExtendedLogger logger, String name, MessageFactory messageFactory) {
		super(logger, name, messageFactory);
	}

	@Override
	public void logMessage(String fqcn, Level level, Marker marker, Message message, Throwable t) {
		super.logMessage(FQCN, level, marker, message, t);
	}

	/**
	 * Sets a log level as a default for a class type. In the absence of a class specific level, the
	 * global default is applied. For a log message to be printed, the log level must be GTE the log
	 * level set for the source class.
	 */
	public static void defLevel(LogLevel level) {
		setLevel(null, level);
	}

	public static void defLevel(String level) {
		setLevel(null, level);
	}

	public static void setLevel(Object source, String level) {
		if (level == null) return;
		level = Strings.titleCase(level);
		setLevel(source, LogLevel.valueOf(level));
	}

	public static void setLevel(Object source, LogLevel level) {
		if (source == null && level == null) return;
		if (source == null) source = LogId;
		if (level == null) level = defaultLogLevel();

		int id = source.hashCode();
		String name = objNameOf(source);

		logLevels.put(id, level);
		if (id == LogId) {
			debug(Log.class, "Default logging level set [level=" + level.toString() + "]");
		} else {
			debug(Log.class, "Class Logging level set [class=" + name + ", level=" + level.toString() + "]");
		}
	}

	private static LogLevel logLevelOf(Object source) {
		if (source == null) return defaultLogLevel();
		LogLevel level = logLevels.get(source.hashCode());
		if (level == null) return defaultLogLevel();
		return level;
	}

	private static LogLevel defaultLogLevel() {
		return logLevels.get(LogId);
	}

	private static boolean loggable(Object source, LogLevel level) {
		LogLevel srcLevel = logLevelOf(source);
		if (level.value() > srcLevel.value()) return false;
		return true;
	}

	private static String objNameOf(Object source) {
		String fqname = source.getClass().getName();
		int dot = fqname.lastIndexOf('.');
		String name = dot > -1 ? fqname.substring(dot + 1) : fqname;
		return name;
	}

	// /////////////////////////////////////////////////////////////////////////

	public static void trace(Object source, String message) {
		log(source, LogLevel.Trace, message, null);
	}

	public static void trace(Object source, String message, Throwable e) {
		log(source, LogLevel.Trace, message, e);
	}

	public static void debug(Object source, String message) {
		log(source, LogLevel.Debug, message, null);
	}

	public static void debug(Object source, String message, Throwable e) {
		log(source, LogLevel.Debug, message, e);
	}

	public static void info(Object source, String message) {
		log(source, LogLevel.Info, message, null);
	}

	public static void info(Object source, String message, Throwable e) {
		log(source, LogLevel.Info, message, e);
	}

	public static void warn(Object source, String message) {
		log(source, LogLevel.Warn, message, null);
	}

	public static void warn(Object source, String message, Throwable e) {
		log(source, LogLevel.Warn, message, e);
	}

	public static void error(Object source, String message) {
		log(source, LogLevel.Error, message, null);
	}

	public static void error(Object source, String message, Throwable e) {
		log(source, LogLevel.Error, message, e);
	}

	public static void fatal(Object source, String message) {
		log(source, LogLevel.Fatal, message, null);
	}

	public static void fatal(Object source, String message, Throwable e) {
		log(source, LogLevel.Fatal, message, e);
	}

	public static void log(Object source, LogLevel lvl, String message) {
		log(source, lvl, message, null);
	}

	private static void log(Object source, LogLevel srcLevel, String message, Throwable e) {
		if (loggable(source, srcLevel)) {
			if (source == null) source = Log.class;
			logger = PrivateManager.getLogger(source.getClass().getName());
			switch (srcLevel) {
				case Trace:
					logger.trace(message, e);
					break;
				case Debug:
					logger.debug(message, e);
					break;
				case Info:
					logger.info(message, e);
					break;
				case Warn:
					logger.warn(message, e);
					break;
				case Error:
					logger.error(message, e);
					break;
				case Fatal:
					logger.fatal(message, e);
					System.exit(1);
					break;
			}
		}
	}

	public static enum LogLevel {
		Fatal(0),
		Error(1),
		Warn(2),
		Info(3),
		Debug(4),
		Trace(5);

		private int value;

		LogLevel(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}

		public boolean check(Object source) {
			if (source == null) return false;
			return this.value <= logLevelOf(source).value;
		}
	}

	private static class PrivateManager extends LogManager {

		public static LoggerContext getContext() {
			return getContext(FQCN, false);
		}

		public static ExtendedLogger getLogger(final String name) {
			return new Log(getContext().getLogger(name), "", null);
		}
	}
}
