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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import net.certiv.antlr.wizard.util.Log;
import net.certiv.antlr.wizard.util.Strings;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Config extends AbstractDstPaths {

	private static final String magicIdValue = "AntlrProjectGen451";
	private static final IOFileFilter antlrJar = new WildcardFileFilter("antlr-4*.jar");
	private static final String AntlrJarName = "antlr-4.4-complete.jar";
	private static final String javaDefalutHome = "C:/Program Files/Java/jre7";
	private static final String configFileSuffix = "Config.json";

	private CliOptions opts; // wrapper for command-line choices
	private Settings json; // the values represented in the persisted file

	public static final FilenameFilter jsonFilter = new FilenameFilter() {

		public boolean accept(File dir, String name) {
			if (name.toLowerCase().endsWith(".json")) return true;
			return false;
		}
	};

	/**
	 * Default constructor required by Gson
	 * 
	 * @param opts
	 */
	public Config() {
		super();
		this.json = new Settings();
	}

	// try and locate a prior persisted settings object; install if found & valid
	public boolean loadPriorSettings(CliOptions opts) throws IOException {
		this.opts = opts;

		// if explicitly specified
		if (opts.flagProjectPath() != null && opts.flagGrammarName() != null) {
			String configPathname = Strings.concat(opts.flagProjectPath(), opts.flagGrammarName()
					+ Config.configFileSuffix);
			if (!loadConfigurationFile(configPathname)) {
				throw new IOException("Failed to load/validate configuration file " + configPathname);
			}

			setProjectPath(opts.flagProjectPath());
			setGrammarName(opts.flagGrammarName());
			return true;
		}

		// hunting...
		String configDir = new File(".").getCanonicalPath();
		if (opts.flagProjectPath() != null) {
			configDir = opts.flagProjectPath();
		}
		File f = new File(configDir);
		if (f.exists() && f.isDirectory()) {
			File[] possibleConfigs = f.listFiles(jsonFilter);
			for (File file : possibleConfigs) {
				if (validSettings(file)) {
					String fn;
					try {
						fn = file.getCanonicalPath();
					} catch (IOException e) {
						throw new IOException("Could not resolve referenced project path directory", e);
					}

					if (loadConfigurationFile(fn)) {
						setProjectPath(opts.flagProjectPath());
						return true;
					}
				}
			}
		}

		Log.warn(this, "Failed to find the configuration file");
		return false;
	}

	// 4) update from any remaining flags and save it
	boolean updateConfigFromArgs() throws IOException {

		String lastError = "Unknown";
		try {
			// --- package name
			if (opts.flagPackageName() != null) {
				setPackageName(opts.flagPackageName());
			}

			// --- java path
			if (opts.flagJavaPath() != null) {
				setJavaPath(opts.flagJavaPath());
			} else if (opts.flagJavaPath() == null) {
				String javaHome = System.getenv("JAVA_HOME");
				if (javaHome == null || javaHome.length() == 0) {
					javaHome = javaDefalutHome;
				}
				File f = new File(Strings.concat(javaHome, "bin"));
				if (f.exists()) {
					lastError = "Failure in determining default Java path";
					String path = f.getCanonicalPath();
					setJavaPath(path);
				}
			}

			// --- internal path
			if (opts.flagSourcePath() != null) {
				setSourcePath(opts.flagSourcePath());
			} else if (getSourcePath() == null) {
				setSourcePath("src");
			}
			if (opts.flagTestPath() != null) {
				setTestPath(opts.flagTestPath());
			} else if (getTestPath() == null) {
				setTestPath("test");
			}

			// --- antlr jar path
			if (opts.flagAntlrPath() != null) {
				setAntlrJarPathName(opts.flagAntlrPath());
			} else {
				if (getAntlrJarPathName() == null) {
					// first try to find the jar in the filesystem starting with the parent file
					Log.info(this, "Trying to find an ANTLR jar to use...");
					File cwd = new File(".");
					List<File> fList = (List<File>) FileUtils.listFiles(cwd, antlrJar, TrueFileFilter.INSTANCE);
					if (!fList.isEmpty()) {
						lastError = "Failure in finding default path to Antlr Jar";
						String antlrJarPath = fList.get(0).getCanonicalPath();
						setAntlrJarPathName(antlrJarPath);
						Log.warn(this, "Guessing " + antlrJarPath);
						Log.warn(this, "Edit the " + getGrammarName() + "Tool.bat file to correct.");
					} else {
						// use the default jar in jar
						setAntlrJarPathName(AntlrJarName);
					}
				}
			}
		} catch (Exception e) {
			throw new IOException(lastError, e);
		}
		return true;
	}

	public boolean loadConfigurationFile(String filename) {
		File file = new File(filename);
		if (!file.exists() && (opts.flagCreate() || opts.flagDescriptors())) {
			json.magicId = magicIdValue;
			return true; // intent is to create
		}
		if (validSettings(file)) {
			try {
				String settings = readFile(filename);
				Gson gson = new Gson();
				json = gson.fromJson(settings, Settings.class);
				deriveDstPaths();
				return true;
			} catch (IOException e) {
				Log.error(this, "Failed to read configuration file " + filename + ": " + e.getMessage());
				return false;
			}
		}
		Log.error(this, "Configuration file " + filename + " is not valid.");
		return false;
	}

	public void saveConfiguration() throws IOException {
		String filename = Strings.concat(getProjectPath(), getGrammarName() + configFileSuffix);
		Gson gson = new GsonBuilder().setVersion(1.0).setPrettyPrinting().create();
		String contents = gson.toJson(json);
		try {
			writeFile(filename, contents);
		} catch (IOException e) {
			Log.error(this, "Failed to save configuration file " + filename, e);
			throw e;
		}
	}

	/**
	 * Verify the persisted settings file contains the correct magic number
	 * 
	 * @param file
	 * @return
	 */
	public boolean validSettings(File file) {
		Gson gson = new Gson();
		try {
			String settings = FileUtils.readFileToString(file);
			Settings tmp = gson.fromJson(settings, Settings.class);
			if (magicIdValue.equals(tmp.magicId)) {
				return true;
			}
		} catch (IOException e) {
			Log.warn(this, "Not a valid configuration file.", e);
		}
		return false;
	}

	public String readFile(String filename) throws IOException {
		File file = new File(filename);
		return FileUtils.readFileToString(file);
	}

	public void writeFile(String filename, String contents) throws IOException {
		Log.info(this, "Checking " + filename);

		File file = new File(filename);
		if (file.exists() && file.isFile()) {
			if (opts.flagForce()) {
				if (!file.delete()) {
					Log.error(this, "Failed to save file " + filename);
					return;
				}
				file = new File(filename);
			} else {
				return;
			}
		}

		Log.info(this, "Creating... " + filename);
		FileUtils.writeStringToFile(file, contents);
	}

	public String getGrammarName() {
		return json.grammarName;
	}

	public void setGrammarName(String grammarName) {
		json.grammarName = grammarName;
	}

	public String getPackageName() {
		return json.packageName;
	}

	public void setPackageName(String packageName) {
		json.packageName = packageName;
		if (packageName != null) {
			json.packagePath = packageName.replace('.', '/');
		}
		if (!json.loaded && packageName != null && packageName.length() > 0) {
			if (json.sourcePath != null && json.sourcePath.length() > 0) {
				json.loaded = true;
				deriveDstPaths();
			}
		}
	}

	@Override
	public String getPackagePath() {
		return json.packagePath;
	}

	@Override
	public String getProjectPath() {
		return json.projectPath;
	}

	public void setProjectPath(String projectPath) {
		json.projectPath = projectPath;
	}

	@Override
	public String getSourcePath() {
		return json.sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		json.sourcePath = sourcePath;
		if (!json.loaded && sourcePath != null && sourcePath.length() > 0) {
			if (json.packageName != null && json.packageName.length() > 0) {
				json.loaded = true;
				deriveDstPaths();
			}
		}
	}

	@Override
	public String getTestPath() {
		return json.testPath;
	}

	public void setTestPath(String testPath) {
		json.testPath = testPath;
	}

	public String getJavaPath() {
		return json.javaPath;
	}

	public void setJavaPath(String javaPath) {
		json.javaPath = javaPath;
	}

	public String getAntlrJarPathName() {
		return json.antlrJarPathName;
	}

	public void setAntlrJarPathName(String antlrJarPathName) {
		json.antlrJarPathName = antlrJarPathName;
	}

	public boolean genDstPaths() {
		if (json.loaded) {
			deriveDstPaths();
			return true;
		}
		return false;
	}

	public boolean isLoaded() {
		return json.loaded;
	}

	public void setLoaded(boolean b) {
		json.loaded = false;
	}
}
