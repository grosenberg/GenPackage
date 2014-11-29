package net.certiv.antlr.project.regen.spec;

import java.io.File;
import java.util.List;

/**
 * The template delimiters & fragment divider symbol strings are source language dependent, so are
 * defined against the instance template group file.
 */
public class Template {

	public String name; 				// destination stg filename (sans ext)
	public List<String> imports;		// stg names for import
	public String delimiter;			// '%' for java; '<>' for batch, etc
	public String divider; 				// regex used to split out unit fragment
	public String marker;				// marker text to allow specialization of the divider

	public transient String pathname;		// constructed template full pathname
	public transient String tmpPathname;	// initial gen dir
	public transient File tmplFile;			// constructed file handle
	public transient String lDelim;			// left delimiter
	public transient String rDelim;			// right delimiter
}
