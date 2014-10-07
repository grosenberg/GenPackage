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
package net.certiv.antlr.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.certiv.antlr.wizard.util.Log;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class CliOptions {

	private Options options;
	private CommandLine cmd;
	private String[] args;
	private boolean valid;

	public CliOptions() {
		super();

		options = new Options();

		// data arguments
		options.addOption("n", "packageName", true, "package name");
		options.addOption("g", "grammarName", true, "grammar name");
		options.addOption("p", "projectPath", true, "system path to project directory");
		options.addOption("s", "sourcePath", true, "internal path to the project source directory");
		options.addOption("t", "testPath", true, "internal path to the project test directory");
		options.addOption("j", "javaPath", true, "system path to java root directory");
		options.addOption("a", "antlrJar", true, "system path to the antlr jar");

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
		formatter.printHelp("GenProject [options]", options);
	}

	public void printHints() {
		System.out.println("Exemplary values: ");
		System.out.println("");
		System.out.println("grammarName is  : 'Json'");
		System.out.println("packageName is  : 'net.certiv.json'");
		System.out.println("");
		System.out.println("projectPath is  : 'D:/DevFiles/Java/WorkSpaces/Main/MyJsonProject'");
		System.out.println("sourcePath is   : 'src'");
		System.out.println("testPath is     : 'test'");
		System.out.println("javaPath is     : 'C:/Program Files/Java/jre7/bin'");
		System.out.println("antlrJarPath is : 'D:/DevFiles/Java/Libs/Antlr/antlr-4.4-complete.jar'");
	}

	public boolean processOptions(String[] args) {
		this.args = args;
		CommandLineParser parser = new PosixParser();
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			Log.error(this, "Command line parse failure: " + e.getMessage());
			return false;
		}
		return true;
	}

	public boolean warnUnrecognized() {
		if (cmd.getArgs().length > 0) { // getArgs is really getUnrecognizedArgs!!!
			Log.warn(this, "Unrecognized Options: " + cmd.getArgList().toString());
			return true;
		}
		return false;
	}

	public int argCount() {
		return cmd.getOptions().length;
	}

	public boolean flagCreate() {
		return cmd.hasOption("c");
	}

	public boolean flagDescriptors() {
		return cmd.hasOption("d");
	}

	public boolean flagHelp() {
		return cmd.hasOption("h");
	}

	public boolean flagHint() {
		return cmd.hasOption("H");
	}

	public boolean flagForce() {
		return cmd.hasOption("f");
	}

	public String flagPackageName() {
		return flag("n");
	}

	public String flagGrammarName() {
		return flag("g");
	}

	public String flagProjectPath() {
		return flag("p");
	}

	public String flagSourcePath() {
		return flag("s");
	}

	public String flagTestPath() {
		return flag("t");
	}

	public String flagJavaPath() {
		return flag("j");
	}

	public String flagAntlrPath() {
		return flag("a");
	}

	private String flag(String opt) {
		if (cmd.hasOption(opt)) {
			return cmd.getOptionValue(opt);
		} else {
			return null;
		}
	}

	public void setDefaultValue(String flag, String value) {
		List<String> argsList = new ArrayList<>(Arrays.asList(args));
		argsList.add(flag);
		argsList.add(value);
		String[] argArray = argsList.toArray(new String[argsList.size()]);
		processOptions(argArray);
	}

	public boolean isVaid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

}
