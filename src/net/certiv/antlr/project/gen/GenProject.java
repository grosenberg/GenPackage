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

import java.io.File;
import java.io.IOException;

import net.certiv.antlr.project.regen.spec.Unit;
import net.certiv.antlr.project.util.Log;
import net.certiv.antlr.project.util.Utils;
import net.certiv.antlr.project.util.Log.LogLevel;

public class GenProject {

	public static void main(String[] args) {
		new GenProject(args);
	}

	public GenProject(String[] args) {
		super();
		Log.defLevel(LogLevel.Debug);

		// evaluate the command line
		GenOptions opts = new GenOptions();
		if (!opts.processOptions(args)) return;
		if (opts.flagHelp()) {
			opts.printUsage();
			return;
		}
		if (opts.flagHint()) {
			opts.printHints();
			return;
		}

		String cwd;
		try {
			cwd = new File(".").getCanonicalPath();
			Log.info(this, "Cwd is " + cwd);
		} catch (IOException e) {
			Log.error(this, "Failure to determine Cwd", e);
			return;
		}

		// create empty configuration
		GenConfig config = new GenConfig(cwd, opts);

		if (opts.flagInit()) {
			if (opts.valProjectPath() == null) {
				Log.fatal(this, "Have to specify a project pathname");
			}
			if (opts.valRuleSetPathname() == null) {
				Log.fatal(this, "Have to specify the rule set pathname");
			}

			String path = opts.valProjectPath();
			File f = new File(path);
			if (f.exists()) {
				if (!f.isDirectory()) {
					Log.fatal(this, "Invalid project pathname: " + path);
				}
			} else {
				Log.debug(this, "Making: " + path);
				if (!Utils.createDirs(path)) {
					Log.fatal(this, "Failed creating project directory: " + path);
				}
			}
			try {
				config.initSettings();
				config.updateSettingsFromArgs();
				config.save();
			} catch (IOException e) {
				Log.fatal(this, "Failed to save initial configurations file", e);
			}

			config.loadRuleSet(opts.valRuleSetPathname());
			Unit unit = config.getRuleSet().units.get("GenProject");
			SrcGenerator srcGen = new SrcGenerator(config, false);
			srcGen.dispatch(unit);
			return;
		}

		// create or load and update configuration
		if (!config.load()) {
			Log.error(this, "Configuration load failed");
			return;
		}

		if (!config.checkSettings()) {
			Log.error(this, "Insufficient command line arguments");
			return;
		}

		// prepare to execute the requested functions
		Productions funcs = new Productions(opts, config);
		if (!funcs.generatedParserExists() && opts.flagDescriptors()) return;

		// create/validate directory structure & generate project
		if (opts.flagCreate() || opts.flagDescriptors() || opts.flagUnitType()) {
			if (!funcs.createDirs()) {
				Log.error(this, "Failure in directory structure creation");
				return;
			}
			Log.info(this, "Directory structure created/validated");

			if (!funcs.generateProject()) {
				Log.error(this, "Project generation failed");
				return;
			}
			Log.info(this, "Project generation completed");
		}
	}
}
