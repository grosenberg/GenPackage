package net.certiv.antlr.project.base;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.certiv.antlr.project.regen.spec.RuleSet;
import net.certiv.antlr.project.regen.spec.Template;
import net.certiv.antlr.project.regen.spec.Unit;
import net.certiv.antlr.project.regen.spec.Variable;
import net.certiv.antlr.project.util.Log;
import net.certiv.antlr.project.util.Strings;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonSyntaxException;

public class ConfigBase {

	public static final String magicIdRules = "ReGen451";
	public static final String modelSuffix = "_model";
	public static final String rulesetExt = ".json";
	public static final String templateGroupExt = ".stg";

	private RuleSet rules;		// the values represented in the persisted file
	private String pathname;	// actual rule set pathname

	public ConfigBase() {
		super();
	}

	public ConfigBase(String pathname) {
		this();
		this.pathname = pathname;
	}

	// ==========================================================================================================
	// RuleSet file loaders & base getters/settters

	public String getRuleSetPathname() {
		return pathname;
	}

	public void setRuleSetPathname(String pathname) {
		this.pathname = pathname;
	}

	public boolean isRuleSetLoaded() {
		return this.rules.loaded;
	}

	public RuleSet getRuleSet() {
		return rules;
	}

	public void setRuleSet(RuleSet rules) {
		this.rules = rules;
	}

	/**
	 * Verify the persisted settings file contains the correct magic number
	 */
	public boolean loadRuleSet(String pathname) {
		try {
			RuleSet rules = (RuleSet) readJsonFile(pathname, RuleSet.class);
			if (!magicIdRules.equals(rules.magicId)) {
				throw new IOException("Wrong magicId - not a valid configuration file: " + pathname);
			}
			this.pathname = pathname;
			this.rules = rules;
			this.rules.loaded = true;
			Log.info(this, "Loaded rule set: " + pathname);
		} catch (IOException | JsonSyntaxException e) {
			Log.debug(this, "Not a valid rule set: " + pathname + " [" + e.getMessage() + "]");
		}
		return this.rules.loaded;
	}

	public Object readJsonFile(String pathname, Class<?> form) throws JsonSyntaxException, IOException {
		File file = new File(pathname);
		if (!file.isFile()) {
			throw new IOException("Not found or not a regular file: " + file.getPath());
		}

		GsonGen gson = new GsonGen();
		gson.configDefaultBuilder();
		gson.create();
		String content = FileUtils.readFileToString(file);
		return gson.fromJson(content, form);
	}

	/**
	 * Saves the data object to a Json file of the given name. Overwrites any existing file. Saves
	 * in a non-compact form.
	 * 
	 * @param filename
	 *            filename of the file to save to
	 * @param obj
	 *            the data object to save
	 * @throws IOException
	 *             thrown if an existing file cannot be overwritten
	 */
	public void saveObj2Json(String filename, Object obj) throws IOException {
		saveObj2Json(filename, obj, false, true);
	}

	public void saveObj2Json(String filename, Object obj, boolean compact, boolean overwrite) throws IOException {
		GsonGen gson = new GsonGen();
		if (compact) {
			gson.configBasicBuilder();
		} else {
			gson.configDefaultBuilder();
		}
		gson.create();
		try {
			String json = gson.toJson(obj);
			writeFile(filename, json, overwrite);
		} catch (IOException | JsonSyntaxException e) {
			Log.error(this, "Failed to save configuration file " + filename, e);
			throw e;
		}
	}

	public void writeFile(String filename, String contents, boolean overwrite) throws IOException {
		File file = new File(filename);
		if (file.exists() && file.isFile()) {
			if (!overwrite) return;
			if (!file.delete()) {
				throw new IOException("Failed to delete preexisting file: " + filename);
			}
		}
		Log.info(this, "Writing to " + filename);
		file = new File(filename);
		FileUtils.writeStringToFile(file, contents);
		Log.debug(this, "Write complete.");
	}

	// ==========================================================================================================
	// RuleSet field getters/settters

	public String getRulesId() {
		return rules.magicId;
	}

	public String getProjectBasePath() {
		return rules.projectBasePath;
	}

	public String getProjectRoot() {
		return rules.projectRoot;
	}

	public String getTemplatePackage() {
		return rules.templatePackage;
	}

	public String getModelGrammar() {
		return rules.modelGrammar;
	}

	public String getModelBasePackage() {
		return rules.modelBasePackage;
	}

	public List<String> getExcludePackages() {
		return rules.excludePackages;
	}

	public String getModelBasePath() {
		return rules.modelBasePath;
	}

	public List<String> getCheckedTypes() {
		return rules.checkedTypes;
	}

	public List<String> getRootTypes() {
		return rules.rootTypes;
	}

	public List<String> getRootDirs() {
		return rules.rootDirs;
	}

	public Map<String, Unit> getUnits() {
		return rules.units;
	}

	public Map<String, Variable> getVariables() {
		return rules.variables;
	}

	public Map<String, Template> getTemplates() {
		return rules.templates;
	}

	// ==========================================================================================================
	// RuleSet helpers

	/**
	 * Returns the units that are of the given type
	 * 
	 * @param uType
	 *            the unit TypeOf to select against
	 * @return
	 */
	public Map<String, Unit> getUnits(TypeOf uType) {
		Map<String, Unit> typedUnits = new HashMap<>();
		Map<String, Unit> units = getUnits();
		for (String key : units.keySet()) {
			Unit unit = units.get(key);
			if (unit.unitType.equals(uType)) {
				typedUnits.put(key, unit);
			}
		}
		return typedUnits;
	}

	/**
	 * Returns the full pathname to a file in the model identified by the given model
	 * 
	 * @param unit
	 * @return
	 */
	public String getUnitPathname(Unit unit) {
		String projectPath = Strings.convertPkgToPath(unit.modelPackage);
		return Strings.concat(rules.modelBasePath, unit.modelRoot, projectPath, unit.modelFilename);
	}

	/**
	 * Returns the full path to the target templates directory
	 */
	public String getTemplatePath() {
		String tpath = Strings.convertPkgToPath(rules.templatePackage);
		return Strings.concat(rules.projectBasePath, rules.projectRoot, tpath);
	}

	/**
	 * Returns the full pathname of the named group fiie in the target templates directory
	 */
	public String getTemplatePathname(String tmplName) {
		String tpath = Strings.convertPkgToPath(rules.templatePackage);
		String tname = Strings.setExtension(tmplName, templateGroupExt);
		return Strings.concat(rules.projectBasePath, rules.projectRoot, tpath, tname);
	}

	/**
	 * Returns the full pathname of a group file created in the given temporary directory
	 * 
	 * @param tmpDir
	 *            the temporary directory to use
	 * @param name
	 *            the filename of the target group file
	 * @return
	 * @throws IOException
	 */
	public String getTmpTmplPathName(File tmpDir, String name) throws IOException {
		String tname = Strings.setExtension(name, templateGroupExt);
		return Strings.concat(tmpDir.getCanonicalPath(), tname);
	}

	/**
	 * Returns the Variable record identified by the given name
	 * 
	 * @param name
	 * @return
	 */
	public Variable getVariable(String name) {
		return rules.variables.get(name);
	}

	/**
	 * Returns the Template record identified by the given name
	 * 
	 * @param name
	 * @return
	 */
	public Template getTemplate(String name) {
		return rules.templates.get(name);
	}
}
