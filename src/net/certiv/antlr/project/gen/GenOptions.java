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
package net.certiv.antlr.project.gen;

import net.certiv.antlr.project.util.Log;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class GenOptions {

	private Options options;
	private CommandLine cli;

	public GenOptions() {
		super();

		options = new Options();

		// data arguments
		options.addOption("n", "package", true, "package name");
		options.addOption("g", "grammar", true, "grammar name");
		options.addOption("p", "project", true, "system path to project directory");
		options.addOption("s", "source", true, "internal path to the project source directory");
		options.addOption("t", "test", true, "internal path to the project test directory");
		options.addOption("j", "java", true, "system path to java home directory");
		options.addOption("a", "antlr", true, "system path to the antlr jar");
		options.addOption("r", "rulesPathname", true, "system path to the project rule set");

		// procedurals
		options.addOption("c", "create", false, "create project files");
		// options.addOption("x", "tools", false, "create tools and basic grammar files");
		options.addOption("d", "descriptors", false, "create descriptor files only");
		options.addOption("h", "help", false, "help: print usage information");
		options.addOption("H", "Hint", false, "Hint: print example usage information");

		options.addOption("f", false, "force overwrite operation (use with caution)");

	}

	public void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("GenProject [options] rulesPathname", options);
	}

	public void printHints() {
		System.out.println("Exemplary values: ");
		System.out.println("");
		System.out.println("grammar name is : 'Json'");
		System.out.println("package name is : 'net.certiv.json'");
		System.out.println("");
		System.out.println("project is      : 'D:/DevFiles/Java/WorkSpaces/Main/MyJsonProject'");
		System.out.println("source is       : 'src'");
		System.out.println("test is         : 'test'");
		System.out.println("java is         : 'C:/Program Files/Java/jre7/bin'");
		System.out.println("antlr is        : 'D:/DevFiles/Java/Libs/Antlr/antlr-4.4-complete.jar'");
		System.out.println("");
		System.out.println("rule set is     : 'D:/DevFiles/Java/Rules/GenProjectRulseSet.json'");
	}

	public boolean processOptions(String[] args) {
		CommandLineParser parser = new PosixParser();
		try {
			cli = parser.parse(options, args);
		} catch (ParseException e) {
			Log.error(this, "Command line parse failure: " + e.getMessage());
			return false;
		}
		return !hasUnrecognized();
	}

	private boolean hasUnrecognized() {
		if (cli.getArgs().length > 0) { // getArgs is really getUnrecognizedArgs!!!
			Log.warn(this, "Unrecognized Options: " + cli.getArgList().toString());
			printUsage();
			return true;
		}
		return false;
	}

	public int argCount() {
		return cli.getOptions().length;
	}

	public boolean flagCreate() {
		return cli.hasOption("c");
	}

	public boolean flagDescriptors() {
		return cli.hasOption("d");
	}

	public boolean flagHelp() {
		return cli.hasOption("h");
	}

	public boolean flagHint() {
		return cli.hasOption("H");
	}

	public boolean flagForce() {
		return cli.hasOption("f");
	}

	public String valPackageName() {
		return value("n");
	}

	public String valGrammarName() {
		return value("g");
	}

	public String valProjectPath() {
		return value("p");
	}

	public String valRuleSetPathname() {
		return value("r");
	}

	public String valSourcePath() {
		return value("s");
	}

	public String valTestPath() {
		return value("t");
	}

	public String valJavaPath() {
		return value("j");
	}

	public String valAntlrPathname() {
		return value("a");
	}

	private String value(String opt) {
		if (cli.hasOption(opt)) {
			return cli.getOptionValue(opt);
		} else {
			return null;
		}
	}
}
