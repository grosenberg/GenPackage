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

import com.google.gson.annotations.Since;

/**
 * Minimal class used to marshal settings to and from a json file.
 * 
 * @author Gbr
 * 
 */
public class Settings {

	@Since(1.0)
	public String magicId = "<not set>"; // not null
	@Since(1.0)
	public boolean loaded = false; // pendantic

	@Since(1.0)
	public String grammarName; // "Metal"
	@Since(1.0)
	public String packageName; // "net.certiv.metal"
	@Since(1.0)
	public String packagePath; // derived from packageName: "net/certiv/metal"

	@Since(1.0)
	public String projectPath; // "D:/DevFiles/Java/WorkSpaces/Main/net.certiv.metal"
	@Since(1.0)
	public String sourcePath; // "src"
	@Since(1.0)
	public String testPath; // "test"
	@Since(1.0)
	public String javaPath; // system path to the jre/jdk bin directory
	@Since(1.0)
	public String antlrJarPathName; // system path and jar name

}
