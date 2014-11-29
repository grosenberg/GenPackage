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
package net.certiv.antlr.project.regen.spec;

import java.util.List;
import java.util.Map;

/**
 * Root class used to marshal template regeneration conversion rules to and from a json file.
 * 
 * @author Gbr
 */
public class RuleSet {

	public String magicId = "<>";			// must not be null

	public String projectBasePath;			// "D:/DevFiles/Java/WorkSpaces/Main/GenProject"
	public String projectRoot; 				// "src"
	public String templatePackage; 			// "net.certiv.antlr.project.templates"

	public String modelGrammar;				// "Json"
	public String modelBasePath;			// "D:/DevFiles/Java/WorkSpaces/Main/GenProjectModel"
	public String modelBasePackage;			// "net.certiv.json"
	public List<String> excludePackages;	// "net.certiv.json.parser.gen"

	// types included when updating
	public List<String> checkedTypes;		// { "java", "g4", "xml", "bat", "sh", "classpaht", "project" };
	// types allowed in root of model path
	public List<String> rootTypes; 			// { "xml", "bat", "sh", "classpaht", "project" };
	// root directories to consider
	public List<String> rootDirs;			// { "src", "test", "lib" };

	// Maps a unique name to a model unit.
	public Map<String, Unit> units;
	// Set of conversion operations to be applied to a named unit (or unit fragment)
	public Map<String, Variable> variables;
	// Defines the template group files to be regenerated from the model.
	public Map<String, Template> templates;

	public transient boolean loaded = false;
}
