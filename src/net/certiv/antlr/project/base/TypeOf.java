package net.certiv.antlr.project.base;

/**
 * May be possible to regenerate a divisible part of the template set. Would have to be exclusive to
 * one or more template files. If it is to work, need to 'type' the units necessary for the partial
 * regeneration.
 */
public enum TypeOf {

	// coding, transformable
	main,
	core,
	util,
	test,
	tool,
	grammar,
	symbol,
	state,

	// non-coding, transformable
	text,
	logXml,

	// coding, transformable, dependent on antlr generated files
	descriptor,
	derived,
	walker,

	// resource, non-transformable
	document,
	binary,

	// anything else
	ignore;
}
