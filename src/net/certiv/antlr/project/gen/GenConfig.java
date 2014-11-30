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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.certiv.antlr.project.base.ConfigBase;
import net.certiv.antlr.project.base.CtxMethod;
import net.certiv.antlr.project.base.GsonGen;
import net.certiv.antlr.project.gen.spec.Settings;
import net.certiv.antlr.project.regen.spec.Unit;
import net.certiv.antlr.project.util.Log;
import net.certiv.antlr.project.util.Strings;
import net.certiv.antlr.project.util.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.gson.JsonSyntaxException;

public class GenConfig extends ConfigBase {

	private static final String magicIdSettings = "AntlrProjectGen451";

	private static final String AntlrJarName = "antlr-4.4-complete.jar";
	private static final String GenProjJarName = "GenProject-2.0-complete.jar";

	static final String configFileSuffix = "GenConfig.json";

	private static final String JavaHome = "JAVA_HOME";
	private static final String javaDefalutHome = "C:/Program Files/java/jre7";
	private static final String ParserGenPath = "parser/gen";
	private static final String parser = "Parser.java";

	private String cwd;
	private GenOptions opts;		// command-line options
	private Settings settings;		// persisted values for customizing the gen production

	// private List<String> contexts;
	private String contextName;						// for generating decorator access methods
	private Map<String, List<CtxMethod>> ctxs; 		// for generating decorator access methods
	private Map<String, List<String>> importCtxs;	// for generating decorator import statements

	public static final FilenameFilter jsonFilter = new FilenameFilter() {

		public boolean accept(File dir, String name) {
			if (name.toLowerCase().endsWith(".json")) return true;
			return false;
		}
	};

	public GenConfig(String cwd, GenOptions opts) {
		super();
		this.cwd = cwd;
		this.opts = opts;
		this.settings = new Settings();
	}

	public boolean load() {
		try {
			loadPriorSettings();
			updateSettingsFromArgs();
			save();
		} catch (IOException e) {
			Log.error(this, "Failed in initial configurations load", e);
			return false;
		}
		return true;
	}

	// try and locate a prior persisted settings object; install if found & valid
	private void loadPriorSettings() throws IOException {

		// if explicitly specified
		if (opts.valConfigName() != null && loadSettingsFile(opts.valConfigName())) return;

		if (opts.valProjectPath() != null && opts.valGrammarName() != null) {
			String configPathname = Strings.concat(opts.valProjectPath(), opts.valGrammarName() + configFileSuffix);
			if (loadSettingsFile(configPathname)) return;
		}

		// hunting...
		String configDir = cwd;
		if (opts.valProjectPath() != null) {
			configDir = opts.valProjectPath();
		}
		File f = new File(configDir);
		if (f.exists() && f.isDirectory()) {
			File[] possibleConfigs = f.listFiles(jsonFilter);
			for (File file : possibleConfigs) {
				String filename = file.getCanonicalPath();
				Settings tmpModel = loadFile(filename, true);
				if (tmpModel != null) {
					settings = tmpModel;
					return;
				}
			}
		}
		Log.warn(this, "Failed to find a configuration file");
	}

	private boolean loadSettingsFile(String filename) {
		File file = new File(filename);
		if (!file.exists() && (opts.flagCreate() || opts.flagDescriptors())) {
			settings.magicId = magicIdSettings;
			return true;
		}
		Settings tmpSettings = loadFile(filename, false);
		if (tmpSettings != null) {
			settings = tmpSettings;
			return true;
		}
		return false;
	}

	private Settings loadFile(String filename, boolean quiet) {
		File file = new File(filename);
		if (!file.exists() || !file.isFile()) {
			return null;
		}
		GsonGen gson = new GsonGen();
		gson.configDefaultBuilder();
		gson.create();
		try {
			String content = FileUtils.readFileToString(file);
			settings = gson.fromJson(content, Settings.class);
			if (magicIdSettings.equals(settings.magicId)) {
				settings.loaded = true;
				return settings;
			}
		} catch (IOException | JsonSyntaxException e) {
			if (!quiet) {
				Log.error(this, "Failed to read configuration file " + filename + ": " + e.getMessage());
			}
		}
		return null;
	}

	public void initSettings() {
		settings.magicId = magicIdSettings;
		settings.loaded = true;
	}

	// update from any remaining flags and save it
	public boolean updateSettingsFromArgs() throws IOException {

		String lastError = "Unknown";
		try {
			// --- grammar name
			if (opts.valGrammarName() != null) {
				setGenGrammarName(opts.valGrammarName());
			}

			// --- package name
			if (opts.valPackageName() != null) {
				setGenPackageName(opts.valPackageName());
			}

			// --- project path
			if (opts.valProjectPath() != null) {
				setGenProjectPath(opts.valProjectPath());
			}

			// --- internal path: src
			if (opts.valSourcePath() != null) {
				setGenSourcePath(opts.valSourcePath());
			} else if (getGenSourcePath() == null) {
				setGenSourcePath("src");
			}

			// --- internal path: test
			if (opts.valTestPath() != null) {
				setGenTestPath(opts.valTestPath());
			} else if (getGenTestPath() == null) {
				setGenTestPath("test");
			}

			// --- java path
			if (opts.valJavaPath() != null) {
				String path = opts.valJavaPath();
				path = FilenameUtils.normalizeNoEndSeparator(path);
				if (path.endsWith("/bin")) {
					path = FilenameUtils.getFullPathNoEndSeparator(path);
				}
				setGenJavaPath(path);
			} else {
				String javaHome = System.getenv("JAVA_HOME");
				if (javaHome == null || javaHome.length() == 0) {
					javaHome = javaDefalutHome;
				}
				File f = new File(Strings.concat(javaHome, "bin"));
				if (f.exists()) {
					lastError = "Failure in determining default Java path";
					String path = f.getParent();
					setGenJavaPath(path);
				}
			}

			// --- antlr jar path
			if (opts.valAntlrPathname() != null) {
				setGenAntlrPathname(opts.valAntlrPathname());
			} else {
				if (getGenAntlrPathname() == null) {
					String pathname = Utils.scanClassPath("^.*?antlr-.*?complete.jar");
					if (pathname == null) {
						Log.warn(this, "Failed to find Antlr jar pathname from classpath");
						pathname = Strings.concat(getGenProjectPath(), "lib", AntlrJarName);
						Log.warn(this, "Guessing " + pathname);
						Log.warn(this, "Edit " + getGenConfigPathname() + " to correct.");
					}
					setGenAntlrPathname(pathname);
				}
			}

			// --- GenProject jar path
			if (opts.valGenProjectJarPathname() != null) {
				setGenProjJarPathname(opts.valGenProjectJarPathname());
			} else {
				if (getGenProjJarPathname() == null) {
					String pathname = Utils.findJarPathname(this.getClass());
					if (pathname == null) {
						pathname = Utils.scanClassPath("^.*?genproject-.*?complete.jar");
						if (pathname == null) {
							Log.warn(this, "Failed to find GenProject jar pathname from classpath");
							pathname = Strings.concat(getGenProjectPath(), "lib", GenProjJarName);
							Log.warn(this, "Guessing " + pathname);
							Log.warn(this, "Edit " + getGenConfigPathname() + " to correct.");
						}
					}
					setGenProjJarPathname(pathname);
				}
			}

			// --- rules set pathname
			if (opts.valRuleSetPathname() != null) {
				String ruleSetPathname = opts.valRuleSetPathname();
				ruleSetPathname = FilenameUtils.normalize(ruleSetPathname);
				lastError = "Failed to find rule set at " + ruleSetPathname;
				File f = new File(ruleSetPathname);
				if (!f.exists()) throw new IOException();
				setRuleSetPathname(ruleSetPathname);
			}

		} catch (Exception e) {
			settings.loaded = false;
			throw new IOException(lastError, e);
		}
		return settings.loaded;
	}

	public void save() throws IOException {
		String filename = Strings.concat(getGenProjectPath(), getGenGrammarName() + configFileSuffix);
		try {
			settings.configPathname = filename;
			normalize();
			saveObj2Json(filename, settings);
		} catch (IOException | JsonSyntaxException e) {
			Log.error(this, "Failed to save configuration file " + filename, e);
			throw e;
		}
	}

	private void normalize() {
		settings.projectPath = FilenameUtils.normalize(settings.projectPath, true);
		settings.sourcePath = FilenameUtils.normalize(settings.sourcePath, true);
		settings.testPath = FilenameUtils.normalize(settings.testPath, true);
		settings.javaPath = FilenameUtils.normalize(settings.javaPath, true);
		settings.antlrPathname = FilenameUtils.normalize(settings.antlrPathname, true);
		settings.genProjJarPathname = FilenameUtils.normalize(settings.genProjJarPathname, true);
		settings.rulesPathname = FilenameUtils.normalize(settings.rulesPathname, true);
		settings.configPathname = FilenameUtils.normalize(settings.configPathname, true);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////

	public String getGenParserPathname() {
		return Strings.concat(
				getGenProjectPath(),
				getGenSourcePath(),
				getGenPackagePath(),
				ParserGenPath,
				getGenGrammarName() + parser);
	}

	public String getGenPathname(Unit unit) {
		String root = unit.modelRoot;
		String pkg = "";
		if (unit.modelPackage.length() > 0) {
			pkg = getModelBasePackage();
			pkg = unit.modelPackage.substring(pkg.length());
			pkg = Strings.convertPkgToPath(getGenPackageName() + pkg);
		}
		String name = unit.modelFilename;
		int idx = name.indexOf("Descriptor");
		if (idx > 0 && getGenContextName() != null) {
			name = getGenContextName() + name.substring(idx);
		} else if (name.startsWith(getRuleSet().modelGrammar)) {
			name = name.replace(getRuleSet().modelGrammar, getGenGrammarName());
		}
		return Strings.concat(getGenProjectPath(), root, pkg, name);
	}

	// check min requirements to consider loaded
	public boolean checkSettings() {
		settings.loaded = false;
		String msg = "Insufficient parameters to build project files";
		if (Utils.checkValues(msg,
				getGenGrammarName(),
				getGenPackageName(),
				getGenProjectPath(),
				getGenSourcePath(),
				getGenJavaPath(),
				getRuleSetPathname()
				)) {
			settings.loaded = true;
		}
		return settings.loaded;
	}

	public String getRuleSetPathname() {
		return settings.rulesPathname;
	}

	public void setRuleSetPathname(String ruleSetPathname) {
		if (ruleSetPathname != null) {
			super.setRuleSetPathname(ruleSetPathname);
			settings.rulesPathname = ruleSetPathname;
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////

	public void setCurrentContextName(String contextName) {
		this.contextName = contextName;
	}

	public Map<String, List<CtxMethod>> getContextMethods() {
		if (ctxs == null) ctxs = new HashMap<>();
		return ctxs;
	}

	public void setContextMethods(Map<String, List<CtxMethod>> ctxs) {
		this.ctxs = ctxs;
	}

	public Map<String, List<String>> getImportContexts() {
		if (importCtxs == null) importCtxs = new HashMap<>();
		return importCtxs;
	}

	public void setImportContexts(Map<String, List<String>> importCtxs) {
		this.importCtxs = importCtxs;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////

	/** Current context name when generating descriptors; null otherwise */
	public String getGenContextName() {
		return contextName;
	}

	/** List of contexts that exist within the scope of a named context */
	public List<String> getGenNamedContexts(String cname) {
		return importCtxs.get(cname);
	}

	/** List of methods that exist within the scope of a named context */
	public List<CtxMethod> getGenContextMethods(String cname) {
		return ctxs.get(cname);
	}

	/** List of all contexts that exist within the scope of a generated parser */
	public Set<String> getGenContextList() {
		return importCtxs.keySet();
	}

	public String getGenGrammarName() {
		return settings.grammarName;
	}

	public void setGenGrammarName(String grammarName) {
		if (grammarName != null) {
			settings.grammarName = grammarName;
		}
	}

	/**
	 * By convention, start rule is the lowercase equivalent of the grammar name
	 * 
	 * @return
	 */
	public String getGenStartRule() {
		return settings.grammarName.toLowerCase();
	}

	public String getGenPackageName() {
		return settings.packageName;
	}

	public void setGenPackageName(String packageName) {
		if (packageName != null) {
			settings.packageName = packageName;
		}
	}

	/**
	 * Convert the package name to a path representation
	 * 
	 * @return
	 */
	public String getGenPackagePath() {
		if (settings.packageName != null) {
			return settings.packageName.replace('.', '/');
		}
		return "";
	}

	/**
	 * System path to (and including) the project root directory
	 * 
	 * @return
	 */
	public String getGenProjectPath() {
		return settings.projectPath;
	}

	public void setGenProjectPath(String projectPath) {
		if (projectPath != null) {
			settings.projectPath = projectPath;
		}
	}

	/**
	 * By convention, the root directory name of the project
	 * 
	 * @return
	 */
	public String getGenProjectName() {
		String name = FilenameUtils.normalizeNoEndSeparator(getGenProjectPath(), true);
		return FilenameUtils.getBaseName(name);
	}

	/**
	 * By convention, the path to the parent directory of the project root directory
	 * 
	 * @return
	 */
	public String getGenWorkspacePath() {
		String name = FilenameUtils.normalizeNoEndSeparator(getGenProjectPath(), true);
		return FilenameUtils.getFullPathNoEndSeparator(name);
	}

	/**
	 * By convention, the path interval between the project root directory to the root directory of
	 * the source package path.
	 * 
	 * @return
	 */
	public String getGenSourcePath() {
		return settings.sourcePath;
	}

	public void setGenSourcePath(String sourcePath) {
		if (sourcePath != null) {
			settings.sourcePath = sourcePath;
		}
	}

	/**
	 * By convention, the path interval between the project root directory to the root directory of
	 * the test package path.
	 * 
	 * @return
	 */
	public String getGenTestPath() {
		return settings.testPath;
	}

	public void setGenTestPath(String testPath) {
		if (testPath != null) {
			settings.testPath = testPath;
		}
	}

	/**
	 * System path to (and including) the Java home directory
	 * 
	 * @return
	 */
	public String getGenJavaPath() {
		if (settings.javaPath == null || settings.javaPath.length() == 0) {
			settings.javaPath = System.getenv(JavaHome);
		}
		if (settings.javaPath == null) {
			settings.javaPath = javaDefalutHome;
		}
		return settings.javaPath;
	}

	public void setGenJavaPath(String javaPath) {
		if (javaPath != null) {
			settings.javaPath = javaPath;
		}
	}

	/** System path to (and including) the complete Antlr jar */
	public String getGenAntlrPathname() {
		return settings.antlrPathname;
	}

	public void setGenAntlrPathname(String antlrJarPathname) {
		if (antlrJarPathname != null) {
			settings.antlrPathname = antlrJarPathname;
		}
	}

	public String getGenProjJarPathname() {
		return settings.genProjJarPathname;
	}

	public void setGenProjJarPathname(String genProjJarPathname) {
		if (genProjJarPathname != null) {
			settings.genProjJarPathname = genProjJarPathname;
		}
	}

	public String getGenConfigPathname() {
		return settings.configPathname;
	}

	public void setGenConfigPathname(String configPathname) {
		if (configPathname != null) {
			settings.configPathname = configPathname;
		}
	}
}
