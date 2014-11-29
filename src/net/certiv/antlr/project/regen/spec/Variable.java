package net.certiv.antlr.project.regen.spec;

import java.util.List;

import net.certiv.antlr.project.base.VarType;

/**
 * Defines conversions from known model strings to value agnostic template variables
 */
public class Variable {

	public String variable; 	// actual template variable name; same as var usually
	public VarType vType;		// op type of this variable

	// for generation
	public String source; 		// reference to file that is source of values
	public String select; 		// regex selector for pulling out list of values

	public transient String value;			// computed single valued vars
	public transient List<String> values;	// computed list valued vars

	// for regen
	public String search;		// search string
	public String replace;		// replacement string
	public String marker;		// marker

	public transient String varText;		// computed replacement variable
	public transient String replacement;	// computed replacement text
}
