package net.certiv.antlr.project.regen.spec;

import java.util.List;

import net.certiv.antlr.project.base.TypeOf;

/**
 * A unit can be an entire file, class, method, or odd bit of code or text. Defines the operations
 * to be performed on the unit and where the result is to be stored. Fragments are expected to be
 * delimited by symbol strings that embed the name of the fragment.
 */
public class Unit {

	public String modelRoot; 			// src_model, etc (suffix "_model" will be removed in regen process)
	public String modelPackage; 		// specified as path from model root
	public String modelFilename;		// file name for this model unit
	public String unitName; 			// name of the template to be created from this unit

	// public List<String> filters = new ArrayList<>(); // filters to remove content
	public boolean primary;				// true => marks files that will be generated

	public TypeOf unitType;				// unit type classifier: descriptive
	public List<String> templateVars;	// unit variables list; defines the applicable substitutions
	public List<String> parts;			// ordered, render as part of this unit
	public boolean literal;				// true => just insert; don't attempt var substitutions

	public String license;				// name of license template
	public boolean copyright;			// header includes copyright notice
	public boolean contributor;			// header includes contributor list
	public boolean description;			// header includes descriptive text
	public boolean version;				// header includes version info

	public String templateGroup; 		// reference to the template group for append

	public transient String m_pathname;	// constructed model full pathname
	public transient String t_pathname;	// constructed template full pathname
	public transient String g_pathname;	// constructed generated file pathname
}
