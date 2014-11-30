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
package net.certiv.antlr.project.regen;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.certiv.antlr.project.base.ConfigBase;
import net.certiv.antlr.project.base.TypeOf;
import net.certiv.antlr.project.base.VarType;
import net.certiv.antlr.project.regen.spec.RuleSet;
import net.certiv.antlr.project.regen.spec.Template;
import net.certiv.antlr.project.regen.spec.Unit;
import net.certiv.antlr.project.regen.spec.Variable;
import net.certiv.antlr.project.util.Log;
import net.certiv.antlr.project.util.Log.LogLevel;
import net.certiv.antlr.project.util.Strings;

import org.apache.commons.io.FilenameUtils;

import com.google.gson.JsonSyntaxException;

public class ReGenConfig extends ConfigBase {

	private ReGenOpts opts;		// cli options

	public static final FilenameFilter rulesetFilter = new FilenameFilter() {

		public boolean accept(File dir, String name) {
			if (name.endsWith(rulesetExt)) return true;
			return false;
		}
	};

	public final FilenameFilter modelFilter = new FilenameFilter() {

		public boolean accept(File dir, String name) {
			if (getRuleSet().rootDirs.contains(name)) return true;
			for (String ext : getRuleSet().rootTypes) {
				if (name.endsWith(ext)) return true;
			}
			return false;
		}
	};

	public ReGenConfig(ReGenOpts opts) {
		super(opts.filepath() + "/" + opts.filename());
		this.opts = opts;
	}

	// ==========================================================================================================
	// ==========================================================================================================

	public File[] findModelRoots(String modelBase) {
		File dir = new File(modelBase);
		File[] modelRoots = null;
		if (dir.exists()) {
			modelRoots = dir.listFiles(modelFilter);
			if (modelRoots.length > 0) {
				Log.info(this, "Found " + modelRoots.length + " model roots.");
			} else {
				Log.fatal(this, "Found no model roots in: " + modelBase);
			}
			return modelRoots;
		}
		Log.fatal(this, "Model base directory does not exist: " + modelBase);
		return modelRoots;
	}

	public String packageOf(String parent, String modelRoot) {
		String pkg = FilenameUtils.normalizeNoEndSeparator(parent, true);
		String bse = FilenameUtils.normalizeNoEndSeparator(modelRoot, true);
		int dot = bse.length() + 1;
		if (dot >= pkg.length()) return "";
		pkg = pkg.substring(dot);
		pkg = pkg.replace('/', '.');
		return pkg;
	}

	/**
	 * Guesses the base package of the model based on the shortest shared, non-zero package name.
	 * Presumes that a single package exists as the root for all model file packages.
	 * 
	 * @param pkg
	 *            pkg to consider
	 * @return new current guess
	 */
	public void basePackage(String pkg) {
		if (pkg == null || pkg.length() == 0) return;
		String base = getRuleSet().modelBasePackage;
		if (base == null || base.length() == 0) {
			getRuleSet().modelBasePackage = pkg;
			return;
		}
		String[] bp = base.split("\\.");
		String[] np = pkg.split("\\.");
		int len = Math.min(bp.length, np.length);
		for (int idx = 0; idx < len; idx++) {
			if (!bp[idx].equals(np[idx])) return;
			if (idx == 0) {
				getRuleSet().modelBasePackage = bp[0];
			} else {
				getRuleSet().modelBasePackage += "." + bp[idx];
			}
		}
	}

	/**
	 * Find (guess) the grammar name from the grammar filename. Antlr places some limits on the
	 * filename, so not a complete guess.
	 * 
	 * @param name
	 *            name of a model file
	 */
	public void findGrammarName(String name) {
		if (name.endsWith(".g4")) {
			int len = 3;
			if (name.endsWith("Lexer.g4")) {
				len = 8;
			} else if (name.endsWith("Parser.g4")) {
				len = 9;
			}
			getRuleSet().modelGrammar = name.substring(0, name.length() - len);
		}
	}

	public boolean isExcludedPackage(String pkg) {
		if (getExcludePackages() != null && getExcludePackages().size() > 0 && getExcludePackages().contains(pkg)) {
			return true;
		}
		return false;
	}

	// ==========================================================================================================

	// try and locate a prior persisted configuration object; install if found & valid
	public boolean loadRuleSet() {

		if (opts.filepath() == null && opts.filename() == null) return false;
		if (opts.filepath() != null && opts.filename() != null) {
			setRuleSetPathname(opts.filepath() + opts.filename());
			return loadRuleSet(getRuleSetPathname());
		}

		// search the filepath directory or current directory; use first valid found
		File dir = new File(".");
		if (opts.filepath() != null) {
			dir = new File(opts.filepath());
		}
		File[] possibleConfigs = dir.listFiles(rulesetFilter);
		for (File f : possibleConfigs) {
			if (loadRuleSet(f.getPath())) {
				return loadRuleSet(getRuleSetPathname());
			}
		}
		return false;
	}

	public void saveRuleSet() throws IOException {
		saveRuleSet(getRuleSetPathname(), getRuleSet(), true);
	}

	public void saveRuleSet(String filename, RuleSet rules, boolean force) throws IOException {
		rules.projectBasePath = FilenameUtils.normalize(rules.projectBasePath, true);
		rules.modelBasePath = FilenameUtils.normalize(rules.modelBasePath, true);
		try {
			saveObj2Json(filename, rules);
		} catch (IOException | JsonSyntaxException e) {
			Log.error(this, "Failed to save configuration file " + filename, e);
			throw e;
		}
	}

	// ==========================================================================================================

	// create an example
	public void createSample() {
		RuleSet rules = new RuleSet();
		rules.magicId = ReGenConfig.magicIdRules;
		rules.projectBasePath = opts.filepath();
		rules.projectRoot = "src";
		rules.templatePackage = "net.certiv.antlr.project.templates";
		rules.modelBasePath = opts.filepath();
		rules.excludePackages = new ArrayList<>();
		rules.excludePackages.add("net.certiv.json.parser.gen");

		rules.templates = new HashMap<>();
		Template template = new Template();
		template.name = "Utils.java";
		template.delimiter = "%";
		template.divider = "// <> ==========";
		template.marker = "<>";
		template.imports = new ArrayList<>();
		template.imports.add("bsd");
		template.imports.add("epl");
		rules.templates.put("Utils", template);

		rules.variables = new HashMap<>();
		Variable sub = new Variable();
		sub.variable = "modelGrammar";
		sub.vType = VarType.MatchVar;
		sub.search = "Json";
		rules.variables.put(sub.variable, sub);

		rules.units = new HashMap<>();
		Unit unit = new Unit();
		unit.unitType = TypeOf.main;
		unit.modelRoot = "src_model";
		unit.modelPackage = "net.certiv.json.util";
		unit.modelFilename = "Strings";
		unit.templateVars = new ArrayList<>();
		unit.templateVars.add("packageName");
		unit.templateVars.add("unitName");
		unit.templateGroup = "Utils";
		rules.units.put("Strings", unit);

		try {
			String filename = opts.filename() != null ? opts.filename() : "SampleRuleSet.json";
			saveRuleSet(opts.filepath() + filename, rules, opts.flagForce());
		} catch (IOException e) {
			Log.error(this, "Failed to save sample configuration", e);
		}
	}

	// ==========================================================================================================
	// Painfully complex and strangely complete set of verification rules for the RuleSet

	public boolean verifyRuleValues() {
		Log.info(this, "Checking config base values");
		boolean ok = checkEq(getRulesId(), magicIdRules, LogLevel.Error, "base", "MagicID");
		ok &= checkEmpty(getProjectBasePath(), LogLevel.Error, "base", "Probject base path");
		ok &= checkEmpty(getProjectRoot(), LogLevel.Error, "base", "Probject root");
		ok &= checkEmpty(getTemplatePackage(), LogLevel.Error, "base", "Template package");

		ok &= checkEmpty(getModelGrammar(), LogLevel.Error, "base", "Model grammar name");
		ok &= checkEmpty(getModelBasePackage(), LogLevel.Error, "base", "Model base package name");
		ok &= checkEmpty(getModelBasePath(), LogLevel.Error, "base", "Model base path");
		ok &= checkNull(getExcludePackages(), LogLevel.Error, "base", "Exclude package list");

		ok &= checkNull(getCheckedTypes(), LogLevel.Error, "base", "Checked types");
		ok &= checkNull(getRootTypes(), LogLevel.Error, "base", "Allowed types in root");
		ok &= checkNull(getRootDirs(), LogLevel.Error, "base", "Checked root dirs");

		ok &= checkMap(getUnits(), LogLevel.Error, "base", "Units map");
		ok &= checkMap(getVariables(), LogLevel.Error, "base", "Variables map");
		ok &= checkMap(getTemplates(), LogLevel.Error, "base", "Template map");
		Log.debug(this, "Base values check complete.");
		return ok;
	}

	// set of helpful accumulators
	List<String> u_pnames = new ArrayList<>(); // unique list of unit parts
	List<String> u_vnames = new ArrayList<>();
	List<String> u_tnames = new ArrayList<>();

	public boolean verifyUnits() {
		Log.info(this, "Checking unit specifications");
		List<String> unitNames = new ArrayList<>();
		Map<String, Unit> units = getUnits();
		boolean ok = true;
		for (String mname : units.keySet()) { // set keys must be unique
			Unit unit = units.get(mname);
			if (unit.unitType == TypeOf.ignore) continue;

			ok &= checkEmpty(unit.modelRoot, LogLevel.Error, mname, "Model root", getRootTypes());
			ok &= checkEmpty(unit.modelPackage, LogLevel.Error, mname, "Model package", getRootTypes());
			ok &= checkEmpty(unit.modelFilename, LogLevel.Error, mname, "Model filename");
			ok &= checkDupl(unitNames, unit.unitName, LogLevel.Error, mname, "Duplicate unit name:");

			ok &= checkNull(unit.unitType, LogLevel.Error, mname, "Unit type");

			ok &= checkNull(unit.templateVars, LogLevel.Error, mname, "Variables");
			if (ok) addUnique(u_vnames, unit.templateVars);
			ok &= checkNull(unit.parts, LogLevel.Error, mname, "Parts");
			if (ok) addUnique(u_pnames, unit.parts);
			ok &= checkEmpty(unit.templateGroup, LogLevel.Error, mname, "Template group name", getRootTypes());
			if (ok) addUnique(u_tnames, unit.templateGroup);
		}
		Log.debug(this, "Unit check complete.");
		return ok;
	}

	public boolean verifyVariables() {
		Log.info(this, "Checking variable specifications");
		List<String> varNames = new ArrayList<>();
		boolean ok = true;
		for (String vname : getVariables().keySet()) {
			Variable var = getVariables().get(vname);
			if (ok) checkDupl(varNames, var.variable, LogLevel.Warn, vname, "Duplicate variable names:");
			ok &= checkNull(var.vType, LogLevel.Error, vname, "Var Type");
			ok &= checkEmpty(var.search, LogLevel.Error, vname, "Search string");
			// TODO: verify variable against reflected config method names
			// ok &= checkNull(var.replace, LogLevel.Warn, vname, "Replace string");
			// ok &= checkNull(var.wrap, LogLevel.Warn, vname, "Wrap string");
			// ok &= checkNull(var.marker, LogLevel.Warn, vname, "Marker string");
		}
		Log.debug(this, "Variables check complete.");
		return ok;
	}

	public boolean verifyTemplateGroups() {
		Log.info(this, "Checking template group specifications");
		List<String> tmplNames = new ArrayList<>();
		boolean ok = true;
		for (String tname : getTemplates().keySet()) {
			Template tmpl = getTemplates().get(tname);
			ok &= checkDupl(tmplNames, tmpl.name, LogLevel.Error, tname, "Duplicate template names:");
			ok &= checkNull(tmpl.imports, LogLevel.Error, tname, "Imports list");
			if (tmpl.delimiter != null) {
				ok &= checkInt(tmpl.delimiter.length(), 1, 2, LogLevel.Error, tname, "Delimiters");
			}
			if (tmpl.divider != null) {
				ok &= checkEmpty(tmpl.divider, LogLevel.Error, tname, "Divider");
				if (ok) ok &= checkContains(tmpl.divider, tmpl.marker, LogLevel.Error, tname, "Marker");
			}
		}
		Log.debug(this, "Template group check complete.");
		return ok;
	}

	public boolean verifyIntegration() {
		Log.info(this, "Consistency cross-check.");

		Set<String> units = getUnits().keySet();
		Set<String> vars = getVariables().keySet();
		Set<String> tmpls = getTemplates().keySet();

		boolean ok = delta(u_vnames, vars, LogLevel.Error, "Units", "Missing variable specs for:");
		ok &= delta(u_tnames, tmpls, LogLevel.Error, "Units", "Missing template group specs for:");
		ok &= delta(u_pnames, units, LogLevel.Error, "Units", "Missing unit part specs for:");
		ok &= delta(vars, u_vnames, LogLevel.Warn, "Variables", "Variable specs declared but not used:");
		ok &= delta(tmpls, u_tnames, LogLevel.Warn, "Templates", "Template groups declared but not used:");
		Log.debug(this, "Cross-check complete.");
		return ok;
	}

	// /////////////////////////////////////////////////////////////////////////////////

	private boolean delta(Collection<String> names1, Collection<String> names2, LogLevel lvl, String xname, String msg) {
		boolean ok = true;
		List<String> d = new ArrayList<String>(names1);
		d.removeAll(names2);
		if (d.size() > 0) ok = log(lvl, xname, msg, Strings.toCsv(d));
		return ok;
	}

	private boolean checkNull(Object field, LogLevel lvl, String xname, String name) {
		if (field == null) return log(lvl, xname, name, "is null.");
		return true;
	}

	private boolean checkInt(int value, int min, int max, LogLevel lvl, String xname, String msg) {
		if (value < min || value > max) {
			return log(lvl, xname, String.valueOf(value), "is out of range for", msg);
		}
		return true;
	}

	private boolean checkEq(String field, String value, LogLevel lvl, String xname, String name) {
		boolean ok = checkNull(xname, lvl, field, name);
		if (ok) ok = checkNull(xname, lvl, value, name);
		if (ok && !field.equals(value)) {
			return log(lvl, xname, name, "is not equal to ", field);
		}
		return ok;
	}

	private boolean checkEmpty(String value, LogLevel lvl, String xname, String field) {
		return checkEmpty(value, lvl, xname, field, null);
	}

	private boolean checkEmpty(String value, LogLevel lvl, String xname, String field, List<String> exceptions) {
		boolean ok = checkNull(value, lvl, xname, field);
		if (ok && value.length() == 0) {
			if (exceptions != null) {
				Unit unit = getUnits().get(xname);
				if (unit != null) {
					String ext = FilenameUtils.getExtension(unit.modelFilename);
					for (String e : exceptions) {
						if (e.equals(ext)) return ok;
					}
				}
			}
			return log(lvl, xname, field, "is empty.");
		}
		return ok;
	}

	private boolean checkMap(Map<String, ? extends Object> map, LogLevel lvl, String xname, String name) {
		boolean ok = true;
		for (String iname : map.keySet()) {
			if (iname == null) {
				ok &= log(lvl, xname, "Map", name, "contains a null key");
			}
			if (iname.length() == 0) {
				ok &= log(lvl, xname, "Map", name, "contains an empty key");
			}
			if (map.get(iname) == null) {
				ok &= log(lvl, xname, "Map", name, ", key", iname, "contains a null value.");
			}
		}
		return ok;
	}

	private boolean checkDupl(List<String> unames, String uname, LogLevel lvl, String xname, String msg) {
		boolean ok = checkNull(uname, lvl, xname, "contains a null key");
		if (ok) ok &= checkContains(unames, uname, lvl, xname, msg);
		return unames.add(uname);
	}

	private boolean checkContains(String text, String term, LogLevel lvl, String xname, String msg) {
		int idx = text.indexOf(term);
		if (idx == -1) return log(lvl, xname, msg, term, "is not present in", text);
		return true;
	}

	private boolean checkContains(List<String> unames, String uname, LogLevel lvl, String xname, String msg) {
		if (unames.contains(uname)) return log(lvl, xname, msg, uname);
		return true;
	}

	private void addUnique(List<String> names, List<String> more) {
		for (String s : more) {
			addUnique(names, s);
		}
	}

	// checkEmpty will error if empty name is not allowed;
	private void addUnique(List<String> names, String name) {
		if ("".equals(name)) return;
		if (names.contains(name)) return;
		names.add(name);
	}

	private boolean log(LogLevel lvl, String... strs) {
		StringBuilder sb = new StringBuilder();
		for (String s : strs) {
			if (sb.length() == 0) {
				sb.append("[" + s + "] ");
			} else {
				if (s.charAt(0) != '.' && s.charAt(0) != ',') sb.append(" ");
				sb.append(s);
			}
		}
		Log.log(this, lvl, sb.toString());
		if (lvl == LogLevel.Error) return false;
		return true;
	}
}
