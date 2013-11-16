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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.certiv.antlr.wizard.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class GenProject {

	private static final String STD_SEPARATOR = "/"; // classpath (and unix) separator)
	private static final String WINDOWS_SEPARATOR = "\\\\"; // Windows separator character.

	private static final Pattern ruleName = Pattern.compile("void enter(\\w*)\\(");
	private static final IOFileFilter antlrJar = new WildcardFileFilter("antlr-4*.jar");
	private static final String AntlrJarName = "antlr-4.0-complete.jar";
	private static final String javaDefalutHome = "C:/Program Files/Java/jre7";
	private static final String templateDir = "net/certiv/antlr/wizard/templates";

	private static final FilenameFilter filter = new FilenameFilter() {

		public boolean accept(File dir, String name) {
			if (name.toLowerCase().endsWith(".json")) return true;
			return false;
		}
	};

	private CliOptions opts;
	private Config config;

	private String lastError = "<none>";
	private String cwd;

	private String dstBase;
	private String dstConverter;
	private String dstDescriptors;
	private String dstGenerator;
	private String dstParser;
	private String dstGen;
	private String dstSymbol;
	private String dstTypes;
	private String dstUtil;

	public static void main(String[] args) {
		new GenProject(args);
	}

	public GenProject(String[] args) {
		super();
		try {
			lastError = "Failure to determine Cwd";
			cwd = new File(".").getCanonicalPath();
			Log.info(this, "Cwd is " + cwd);

			// 1) process cli options
			opts = new CliOptions();
			boolean parsed = opts.processOptions(args);

			if (!parsed || opts.flagHelp() || opts.warnUnrecognized()) {
				opts.printUsage();
				return;
			} else if (opts.flagHint()) {
				opts.printHints();
				return;
			}

			// 2) load the config with an empty settings object
			config = new Config(opts);

			// 3) try and locate a persisted settings object; install if found & valid
			if (opts.flagProjectPath() != null && opts.flagGrammarName() != null) {
				String configPathname = concat(opts.flagProjectPath(), opts.flagGrammarName() + Config.configFileSuffix);
				if (!config.loadConfigurationFile(configPathname)) {
					Log.error(this, "Failed to load/validate configuration file " + configPathname);
					return;
				}
				config.setProjectPath(opts.flagProjectPath());
				config.setGrammarName(opts.flagGrammarName());
			} else {
				lastError = "Could not resolve current directory.";
				String configDir = new File(".").getCanonicalPath();
				if (opts.flagProjectPath() != null) {
					configDir = opts.flagProjectPath();
				}
				File f = new File(configDir);
				if (f.exists() && f.isDirectory()) {
					File[] possibleConfigs = f.listFiles(filter);
					for (File file : possibleConfigs) {
						if (config.validSettings(file)) {
							lastError = "Could not resolve project path directory";
							String fn = file.getCanonicalPath();
							if (!config.loadConfigurationFile(fn)) {
								Log.warn(this, "Failed to load/validate configuration file " + fn);
							} else {
								config.setProjectPath(opts.flagProjectPath());
								break;
							}
						}
					}
				}
				if (!config.getLoaded()) {
					Log.error(this, "No configuration file found");
					return;
				}
			}

			// 4) got one; update from any remaining flags and save it

			// --- package name
			if (opts.flagPackageName() != null) {
				config.setPackageName(opts.flagPackageName());
			}

			// --- java path
			if (opts.flagJavaPath() != null) {
				config.setJavaPath(opts.flagJavaPath());
			} else if (opts.flagJavaPath() == null) {
				String javaHome = System.getenv("JAVA_HOME");
				if (javaHome == null || javaHome.length() == 0) {
					javaHome = javaDefalutHome;
				}
				File f = new File(concat(javaHome, "bin"));
				if (f.exists()) {
					lastError = "Failure in determining default Java directory";
					String path = f.getCanonicalPath();
					config.setJavaPath(path);
				}
			}

			// --- internal path
			if (opts.flagSourcePath() != null) {
				config.setSourcePath(opts.flagSourcePath());
			} else if (config.getSourcePath() == null) {
				config.setSourcePath("src");
			}

			// --- antlr jar path
			if (opts.flagAntlrPath() != null) {
				config.setAntlrJarPathName(opts.flagAntlrPath());
			} else {
				if (config.getAntlrJarPathName() == null) {
					// first try to find the jar in the filesystem starting with the parent file
					File cwd = new File(".");
					List<File> fList = (List<File>) FileUtils.listFiles(cwd, antlrJar, TrueFileFilter.INSTANCE);
					if (!fList.isEmpty()) {
						lastError = "Failure in finding default path to Antlr Jar";
						String antlrJarPath = fList.get(0).getCanonicalPath();
						config.setAntlrJarPathName(antlrJarPath);
					} else {
						// use the default jar in jar
						config.setAntlrJarPathName(AntlrJarName);
					}
				}
			}

			// 5) process procedural flag(s)
			// c - conditionally create output
			if (opts.flagCreate()) {
				String msg = "Insufficient parameters to create directories";
				if (checkValues(msg, config.getProjectPath(), config.getSourcePath(), config.getPackageName())) {

					lastError = "Failure in directory structure creation";
					createDirectoryStructure(config.getProjectPath(), config.getSourcePath(), config.getPackagePath());
				}

				config.saveConfigurationFile(concat(config.getProjectPath(), config.getGrammarName()
						+ Config.configFileSuffix));

				msg = "Insufficient parameters to build project files: check the json config file.";
				if (checkValues(msg, config.getProjectPath(), config.getSourcePath(), config.getPackageName(),
						config.getAntlrJarPathName(), config.getJavaPath(), config.getGrammarName())) {

					lastError = "Failure in processing tools creation";
					createTools(config.getProjectPath(), config.getSourcePath(), config.getPackagePath(),
							config.getAntlrJarPathName(), config.getJavaPath(), config.getGrammarName());
					createUtils(config.getPackageName());

					createSymbols(config.getPackageName());
					createTypes(config.getPackageName());

					lastError = "Failure in processing parser creation";
					createParser(config.getPackageName(), config.getGrammarName());

					File fParser = new File(concat(dstGen, config.getGrammarName() + "Parser.java"));
					if (fParser.exists()) {

						lastError = "Failure in parsing the base listener";
						List<String> genNames = parseBaseListener(config.getGrammarName());

						lastError = "Failure in processing descriptors creation";
						createBaseClasses(config.getPackageName(), config.getGrammarName());
						createWalkerPhase(config.getPackageName(), config.getGrammarName(), genNames);

						lastError = "Failure in processing descriptors creation";
						createDescritors(config.getPackageName(), config.getGrammarName(), genNames);
						createGenerator(config.getPackageName(), config.getGrammarName());

					} else {
						Log.info(this, "Generate the Parser, etc. then re-run this tool");
					}
				}
			}
		} catch (Exception e) {
			Log.error(this, lastError, e);
			return;
		}
	}

	private boolean checkValues(String reason, String... values) {
		for (String value : values) {
			if (value == null || value.length() == 0) {
				Log.warn(this, reason);
				return false;
			}
		}
		return true;
	}

	// ///////////////////////////////////////////////////////////////////////////

	public List<String> parseBaseListener(String grammarName) throws IOException {
		List<String> genNames = new ArrayList<>();
		String baseListener = config.readFile(concat(dstGen, grammarName + "ParserBaseListener.java"));
		Matcher m = ruleName.matcher(baseListener);
		while (m.find()) {
			String genName = m.group(1);
			if (!"EveryRule".equals(genName)) {
				genNames.add(genName);
			}
		}
		return genNames;
	}

	// ///////////////////////////////////////////////////////////////////////////

	public boolean createDirectoryStructure(String projectPath, String sourcePath, String packagePath) {

		dstBase = concat(projectPath, sourcePath, packagePath);
		dstConverter = concat(dstBase, "converter");
		dstDescriptors = concat(dstBase, "converter", "descriptors");
		dstParser = concat(dstBase, "parser");
		dstGen = concat(dstBase, "parser", "gen");
		dstGenerator = concat(dstBase, "generator");
		dstSymbol = concat(dstBase, "symbol");
		dstTypes = concat(dstBase, "types");
		dstUtil = concat(dstBase, "util");

		return createDirs(dstBase)
				&& createDirs(dstDescriptors)
				&& createDirs(dstGen)
				&& createDirs(dstGenerator)
				&& createDirs(dstSymbol)
				&& createDirs(dstTypes)
				&& createDirs(dstUtil);
	}

	private String concat(String... args) {
		String result = "";
		for (String arg : args) {
			result = FilenameUtils.concat(result, arg);
		}
		return result;
	}

	/*
	 * Convert separators so the string is a valid URL appropriate for classpath discovery
	 */
	private String concatAsClassPath(String... args) {
		return concat(args).replaceAll(WINDOWS_SEPARATOR, STD_SEPARATOR);
	}

	private boolean createDirs(String dir) {
		File f = new File(dir);
		if (!f.mkdirs()) {
			if (f.exists()) {
				Log.info(this, "Directory exists: " + dir);
				return true;
			}
			Log.error(this, "Failed to make directory for " + dir);
			return false;
		}
		return true;
	}

	// ///////////////////////////////////////////////////////////////////////////

	public void createWalkerPhase(String packageName, String grammarName, List<String> genNames) throws IOException {
		createFirstWalkerPhase(packageName, grammarName, "01");
		createWalkerPhase(packageName, grammarName, "02", genNames);
		createWalkerPhase(packageName, grammarName, "03", genNames);
		createPhaseState(packageName);
	}

	private void createFirstWalkerPhase(String packageName, String grammarName, String phaseNumber) throws IOException {
		STGroup group = new STGroupFile(concatAsClassPath(templateDir, "PhaseClasses.stg"));

		// use grammar name to define presumptive first rule name
		String genName = grammarName.substring(0, 1).toUpperCase();
		genName += grammarName.substring(1).toLowerCase();

		ST st = group.getInstanceOf("FirstPhaseClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		st.add("genName", genName);
		st.add("phaseNumber", phaseNumber);
		String result = st.render();
		config.writeFile(concat(dstConverter, grammarName + "Phase" + phaseNumber + ".java"), result);
	}

	private void createWalkerPhase(String packageName, String grammarName, String phaseNumber, List<String> genNames)
			throws IOException {
		STGroup group = new STGroupFile(concatAsClassPath(templateDir, "PhaseClasses.stg"));
		ST st = group.getInstanceOf("PhaseClassHeader");
		st.add("packageName", packageName);
		StringBuilder result = new StringBuilder();
		result.append(st.render());

		st = group.getInstanceOf("PhaseClassImport1");
		st.add("packageName", packageName);
		for (String genName : genNames) {
			st.add("genName", genName);
			result.append(st.render());
			st.remove("genName");
		}

		st = group.getInstanceOf("PhaseClassImport2");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		for (String genName : genNames) {
			st.add("genName", genName);
			result.append(st.render());
			st.remove("genName");
		}

		st = group.getInstanceOf("PhaseClassClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		st.add("phaseNumber", phaseNumber);
		result.append(st.render());

		st = group.getInstanceOf("PhaseClassConstructor");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		st.add("phaseNumber", phaseNumber);
		result.append(st.render());

		st = group.getInstanceOf("PhaseClassEnterMethod");
		for (String genName : genNames) {
			st.add("genName", genName);
			result.append(st.render());
			st.remove("genName");
		}

		st = group.getInstanceOf("PhaseClassExitMethod");
		for (String genName : genNames) {
			st.add("genName", genName);
			result.append(st.render());
			st.remove("genName");
		}

		st = group.getInstanceOf("PhaseClassTrailer");
		result.append(st.render());

		config.writeFile(concat(dstConverter, grammarName + "Phase" + phaseNumber + ".java"), result.toString());
	}

	private void createPhaseState(String packageName) throws IOException {
		STGroup group = new STGroupFile(concatAsClassPath(templateDir, "PhaseClasses.stg"));
		ST st = group.getInstanceOf("PhaseStateClass");
		st.add("packageName", packageName);
		String result = st.render();
		config.writeFile(concat(dstConverter, "PhaseState.java"), result);
	}

	public void createBaseClasses(String packageName, String grammarName) throws IOException {

		STGroup group = new STGroupFile(concatAsClassPath(templateDir, "BaseClasses.stg"));
		ST st = group.getInstanceOf("ConverterClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		st.add("startRule", grammarName.toLowerCase());
		String result = st.render();
		config.writeFile(concat(dstConverter, "Converter.java"), result);

		st = group.getInstanceOf("PhaseBaseClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(concat(dstConverter, grammarName + "PhaseBase.java"), result);
	}

	public void createParser(String packageName, String grammarName) throws IOException {
		STGroup group = new STGroupFile(concatAsClassPath(templateDir, "Parser.stg"));
		ST st = group.getInstanceOf("ParserGrammar");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		st.add("startRule", grammarName.toLowerCase());
		String result = st.render();
		config.writeFile(concat(dstParser, grammarName + "Parser.g4"), result);

		st = group.getInstanceOf("LexerGrammar");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(concat(dstParser, grammarName + "Lexer.g4"), result);

		st = group.getInstanceOf("LexerAdapterClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(concat(dstParser, "LexerAdaptor.java"), result);

		st = group.getInstanceOf("LexerHelperClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(concat(dstParser, "LexerHelper.java"), result);

		st = group.getInstanceOf("ErrorListenerClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(concat(dstParser, grammarName + "ErrorListener.java"), result);

		st = group.getInstanceOf("LexerErrorStrategyClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(concat(dstParser, grammarName + "LexerErrorStrategy.java"), result);

		st = group.getInstanceOf("ParserErrorStrategyClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(concat(dstParser, grammarName + "ParserErrorStrategy.java"), result);

		st = group.getInstanceOf("TokenClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(concat(dstParser, grammarName + "Token.java"), result);

		st = group.getInstanceOf("TokenFactoryClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(concat(dstParser, grammarName + "TokenFactory.java"), result);

	}

	public void createGenerator(String packageName, String grammarName) throws IOException {
		STGroup group = new STGroupFile(concatAsClassPath(templateDir, "FileGen.stg"));

		ST st = group.getInstanceOf("OutputFileGen");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		String result = st.render();
		config.writeFile(concat(dstGenerator, grammarName + "FileGen.java"), result);

		st = group.getInstanceOf("IOProcessorClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(concat(dstGenerator, "IOProcessor.java"), result);
	}

	public void createDescritors(String packageName, String grammarName, List<String> descriptorNames)
			throws IOException {
		STGroup group = new STGroupFile(concatAsClassPath(templateDir, "DescriptorClasses.stg"));
		ST st = group.getInstanceOf("IDescriptorClass");
		st.add("packageName", packageName);
		String result = st.render();
		config.writeFile(concat(dstConverter, "IDescriptor.java"), result);

		st = group.getInstanceOf("BaseDescriptorClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(concat(dstConverter, "BaseDescriptor.java"), result);

		st = group.getInstanceOf("DescriptorClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		for (String genName : descriptorNames) {
			st.add("genName", genName);
			result = st.render();
			config.writeFile(concat(dstDescriptors, genName + "Descriptor.java"), result);
			st.remove("genName");
		}

		st = group.getInstanceOf("ValueClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(concat(dstConverter, "Value.java"), result);
	}

	public void createTools(String projectPath, String sourcePath, String packagePath, String antlrJarPath,
			String javaPath, String grammarName) throws IOException {
		STGroup group = new STGroupFile(concatAsClassPath(templateDir, "Tools.stg"));
		ST st = group.getInstanceOf("ToolBatchFile");
		st.add("projectPath", projectPath);
		st.add("sourcePath", sourcePath);
		st.add("packagePath", packagePath);
		st.add("antlrJarPath", antlrJarPath);
		st.add("javaPath", javaPath);
		st.add("grammarName", grammarName);
		String result = st.render();
		config.writeFile(concat(dstParser, grammarName + "Tool.bat"), result);
	}

	public void createSymbols(String packageName) throws IOException {
		STGroup group = new STGroupFile(concatAsClassPath(templateDir, "Symbol.stg"));
		ST st = group.getInstanceOf("SymbolClass");
		st.add("packageName", packageName);
		String result = st.render();
		config.writeFile(concat(dstSymbol, "Symbol.java"), result);

		st = group.getInstanceOf("ScopeClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(concat(dstSymbol, "Scope.java"), result);

		st = group.getInstanceOf("SymbolTableClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(concat(dstSymbol, "SymbolTable.java"), result);
	}

	public void createTypes(String packageName) throws IOException {
		STGroup group = new STGroupFile(concatAsClassPath(templateDir, "Types.stg"));
		ST st = group.getInstanceOf("ValueTypeClass");
		st.add("packageName", packageName);
		String result = st.render();
		config.writeFile(concat(dstTypes, "ValueType.java"), result);

		st = group.getInstanceOf("StyleTypeClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(concat(dstTypes, "StyleType.java"), result);

		st = group.getInstanceOf("ContentClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(concat(dstTypes, "Content.java"), result);

		st = group.getInstanceOf("ScopeTypeClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(concat(dstTypes, "ScopeType.java"), result);

		st = group.getInstanceOf("StmtTypeClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(concat(dstTypes, "StmtType.java"), result);

		st = group.getInstanceOf("OpClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(concat(dstTypes, "Op.java"), result);

	}

	public void createUtils(String packageName) throws IOException {
		STGroup group = new STGroupFile(concatAsClassPath(templateDir, "Utils.stg"));
		ST st = group.getInstanceOf("LogClass");
		st.add("packageName", packageName);
		String result = st.render();
		config.writeFile(concat(dstUtil, "Log.java"), result);

		st = group.getInstanceOf("ReflectClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(concat(dstUtil, "Reflect.java"), result);

		st = group.getInstanceOf("StringsClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(concat(dstUtil, "Strings.java"), result);
	}
}
