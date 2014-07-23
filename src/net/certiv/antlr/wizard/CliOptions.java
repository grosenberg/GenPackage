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

	public CliOptions() {
		super();

		options = new Options();

		// data arguments
		options.addOption("n", "packageName", true, "package name");
		options.addOption("g", "grammarName", true, "grammar name");
		options.addOption("p", "projectPath", true, "system path to project directory");
		options.addOption("s", "sourcePath", true, "internal path to the project source directory");
		options.addOption("j", "javaPath", true, "system path to java root directory");
		options.addOption("a", "antlrJar", true, "system path to the antlr jar");

		// procedurals
		options.addOption("c", "create", false, "create project files");
		// options.addOption("t", "tools", false, "create tools and basic grammar files");
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
		System.out.println("javaPath is     : 'C:/Program Files/Java/jre7/bin'");
		System.out.println("antlrJarPath is : 'D:/DevFiles/Java/Libs/Antlr/antlr-4.2-complete.jar'");
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

	public boolean flagTools() {
		return cmd.hasOption("t");
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

}
