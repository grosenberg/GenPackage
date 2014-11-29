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

import net.certiv.antlr.project.util.Log;
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

		// create or load and update configuration
		GenConfig config = new GenConfig(cwd, opts);
		config.getGenProjPathname();
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
		if (opts.flagCreate() || opts.flagDescriptors()) {
			if (!funcs.createDirs()) {
				Log.error(this, "Failure in directory structure creation");
				return;
			}
			Log.info(this, "Directory structure created/validated");

			if (!funcs.generateProject()) {
				Log.error(this, "Project generation failed");
				return;
			}
			Log.error(this, "Project generation completed");
		}
	}
}
