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

import java.io.File;
import java.io.IOException;

import net.certiv.antlr.wizard.util.Log;
import net.certiv.antlr.wizard.util.Strings;

import org.apache.commons.io.FilenameUtils;

public class GenProject {

	public static void main(String[] args) {
		new GenProject(args);
	}

	public GenProject(String[] args) {
		super();

		// evaluate the command line
		CliOptions opts = processCliOpts(args);
		if (!opts.isVaid()) return;

		// create or load and update configuration
		Config config = loadConfiguration(opts);
		if (!config.isLoaded()) {
			Log.error(this, "Configuration load failed");
			return;
		}

		// prepare to execute the requested functions
		Productions funcs = new Productions(opts, config);

		if (funcs.validBuildConfiguration()) {
			config.deriveDstPaths();
		} else {
			Log.error(this, "Insufficient command line arguments");
		}

		// create/validate directory structure
		if (opts.flagCreate() || opts.flagDescriptors()) {
			if (funcs.createDirectories()) {
				Log.info(this, "Directory structure created/validated");
			} else {
				Log.error(this, "Failure in directory structure creation");
				return;
			}
		}

		// create basic files & utility programs
		if (opts.flagCreate()) {
			funcs.createStandardTools();
			funcs.createDefaultParser();

			try {
				File f = new File("Run.bat");
				if (f.isFile()) {
					String proj = FilenameUtils.getName(config.getProjectPath());
					String contents = config.readFile("Run.bat");
					config.writeFile(Strings.concat(config.getProjectPath(), proj + "Run.bat"), contents);
				}
			} catch (IOException e) {
				Log.error(this, "Failed to create the project build bat file");
			}
		}

		if (funcs.generatedParserExists()) {
			if (opts.flagCreate()) {
				funcs.createStandardParserTools();
				funcs.createBaseDescriptor();
			}

			if (opts.flagCreate() || opts.flagDescriptors()) {
				funcs.createDescriptors();
			}

		} else {
			Log.info(this, "A generated parser is required to proceed... ");
		}
	}

	private CliOptions processCliOpts(String[] args) {
		CliOptions opts = new CliOptions();
		String cwd;
		try {
			cwd = new File(".").getCanonicalPath();
		} catch (IOException e) {
			Log.error(this, "Failure to determine Cwd", e);
			return opts;
		}

		Log.info(this, "Cwd is " + cwd);
		boolean parsed = opts.processOptions(args);

		if (!parsed || opts.flagHelp() || opts.warnUnrecognized()) {
			opts.printUsage();
			return opts;
		} else if (opts.flagHint()) {
			opts.printHints();
			return opts;
		}
		opts.setValid(true);
		return opts;
	}

	private Config loadConfiguration(CliOptions opts) {

		Config config = new Config();
		if (opts.isVaid()) {
			try {
				config.loadPriorSettings(opts);
				config.updateConfigFromArgs();
				config.deriveDstPaths();
				config.saveConfiguration();
			} catch (IOException e1) {
				Log.error(this, "Config settings problem", e1);
				config.setLoaded(false);
			}
		}
		return config;
	}
}
