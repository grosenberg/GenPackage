/*
 * [The "BSD license"]
 *  Copyright (c) 2012 Gerald Rosenberg, Certiv Analytics
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.certiv.antlr.wizard;

import java.io.File;
import java.io.IOException;

import net.certiv.antlr.wizard.util.Log;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Config {

	private static final String magicIdValue = "AntlrProjectGen451";

	public static final String configFileSuffix = "Config.json";

	private CliOptions opts;
	private Settings json;

	/**
	 * Default constructor required by Gson
	 * 
	 * @param opts
	 */
	public Config(CliOptions opts) {
		super();
		this.opts = opts;
		this.json = new Settings();
	}

	public boolean loadConfigurationFile(String filename) {
		File file = new File(filename);
		if (!file.exists() && (opts.flagCreate() || opts.flagTools() || opts.flagDescriptors())) {
			return true; // intent is to create
		}
		if (validSettings(file)) {
			try {
				String settings = readFile(filename);
				Gson gson = new Gson();
				json = gson.fromJson(settings, Settings.class);
				return true;
			} catch (IOException e) {
				Log.error(this, "Failed to read configuration file " + filename + ": " + e.getMessage());
				return false;
			}
		}
		Log.error(this, "Configuration file " + filename + " is not valid.");
		return false;
	}

	public boolean saveConfigurationFile(String filename) {
		Gson gson = new GsonBuilder().setVersion(1.0).setPrettyPrinting().create();
		String contents = gson.toJson(json);
		try {
			writeFile(filename, contents);
		} catch (IOException e) {
			Log.error(this, "Failed to save configuration file " + filename, e);
			return false;
		}
		return true;
	}

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
		json.magicId = magicIdValue;
		json.loaded = true;
		json.grammarName = grammarName;
	}

	public String getPackageName() {
		return json.packageName;
	}

	public void setPackageName(String packageName) {
		json.magicId = magicIdValue;
		json.loaded = true;
		json.packageName = packageName;
		json.packagePath = packageName.replace('.', '/');
	}

	public String getPackagePath() {
		return json.packagePath;
	}

	public String getProjectPath() {
		return json.projectPath;
	}

	public void setProjectPath(String projectPath) {
		json.magicId = magicIdValue;
		json.loaded = true;
		json.projectPath = projectPath;
	}

	public String getSourcePath() {
		return json.sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		json.magicId = magicIdValue;
		json.loaded = true;
		json.sourcePath = sourcePath;
	}

	public String getJavaPath() {
		return json.javaPath;
	}

	public void setJavaPath(String javaPath) {
		json.magicId = magicIdValue;
		json.loaded = true;
		json.javaPath = javaPath;
	}

	public String getAntlrJarPathName() {
		return json.antlrJarPathName;
	}

	public void setAntlrJarPathName(String antlrJarPathName) {
		json.magicId = magicIdValue;
		json.loaded = true;
		json.antlrJarPathName = antlrJarPathName;
	}

	public boolean getLoaded() {
		return json.loaded;
	}
}
