package net.certiv.antlr.project.regen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.certiv.antlr.project.base.ConfigBase;
import net.certiv.antlr.project.base.TypeOf;
import net.certiv.antlr.project.regen.spec.Template;
import net.certiv.antlr.project.regen.spec.Unit;
import net.certiv.antlr.project.regen.spec.Variable;
import net.certiv.antlr.project.util.Log;
import net.certiv.antlr.project.util.Log.LogLevel;
import net.certiv.antlr.project.util.Strings;
import net.certiv.antlr.project.util.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class ReGen {

	private static final String CONTENT = "Content";
	private static final String CONTRIBUTOR = "Contributor";
	private static final String COPYRIGHT = "Copyright";
	private static final String DESCRIPTION = "Description";
	private static final String VERSION = "Version";

	private ReGenConfig config;
	private File tmpDir = null;

	public static void main(String[] args) {
		new ReGen(args);
	}

	public ReGen(String[] args) {

		ReGenOpts opts = new ReGenOpts();
		if (!opts.processOptions(args) || opts.flagHelp()) {
			opts.printUsage();
			return;
		}

		Log.debug(this, "Configuration file: " + opts.filename());

		if (opts.flagQuiet()) {
			Log.defLevel(LogLevel.Warn);
		} else if (opts.flagDebug()) {
			Log.defLevel(LogLevel.Debug);
		} else {
			Log.defLevel(LogLevel.Info);
		}

		config = new ReGenConfig(opts);
		if (opts.flagCreate()) {
			Log.info(this, "Creating sample rule set.");
			config.createSample();
			return;
		}

		if (!config.loadRuleSet()) {
			Log.fatal(this, "Rule set load failed; cannot continue");
			return;
		}

		// check for update flag
		Map<String, Unit> units = config.getUnits();
		if (opts.flagUpdate()) {
			Log.info(this, "Updating rule set from the existing model files");
			updateRuleSet(units);
			Log.info(this, "Update completed");
		}

		// check for verify flag
		if (opts.flagVerify()) {
			Log.info(this, "Beginning verification.");
			boolean ok = config.verifyRuleValues();
			ok &= config.verifyUnits();
			ok &= config.verifyTemplateGroups();
			ok &= config.verifyVariables();
			ok &= config.verifyIntegration();
			if (ok) {
				Log.info(this, "Verification complete.");
			} else {
				Log.error(this, "Verification failed; fix the config and re-verify.");
				return;
			}
		}

		// rebuild the templates from the model
		if (opts.flagRebuild()) {
			Log.info(this, "Generating templates");
			boolean ok = processModelToTemplates(units); // build templates in tmp dir
			Log.debug(this, "Regeneration to tmp dir: " + (ok ? "succeeded." : "failed."));
			if (ok) {
				try { // move tmp tmplates to final location
					String tmplDir = config.getTemplatePath();
					Log.debug(this, "Moving regenerated files to: " + tmplDir);
					Utils.moveAllFiles(tmpDir, new File(tmplDir));
				} catch (IOException e) {
					Log.error(this, "Move after regeneration failed.", e);
					ok = false;
				}
			}
			if (ok) {
				Log.info(this, "Regeneration complete.");
			} else {
				Log.error(this, "Regeneration failed.");
			}
		}
	}

	// ====================================================================

	private void updateRuleSet(Map<String, Unit> units) {
		int existing = units.size();
		int pastPrime = 0;
		int primary = 0;
		for (Unit u : units.values()) {
			if (u.primary) pastPrime++;
			u.primary = false;
		}

		File[] modelRoots = config.findModelRoots(config.getModelBasePath());
		for (File root : modelRoots) {
			if (root.isFile()) {
				primary += evaluateUnit(units, null, root);
			} else if (root.isDirectory()) {
				int num = config.getCheckedTypes().size();
				String[] checkedTypes = config.getCheckedTypes().toArray(new String[num]);
				for (File file : FileUtils.listFiles(root, checkedTypes, true)) {
					primary += evaluateUnit(units, root, file);
				}
			}
		}
		int nowNon = units.size() - primary;
		int netPrimary = primary - pastPrime;
		int netNon = nowNon - (existing - pastPrime);
		Log.info(this, "Units: " + units.size() +
				" [primary=" + primary + ", non=" + nowNon + "]"
				+ " (net=" + netPrimary + "/" + netNon + ")");
		Log.info(this, "Base model package identified as: " + config.getRuleSet().modelBasePackage);
		Log.info(this, "Model grammar identified as: " + config.getRuleSet().modelGrammar);
		try {
			config.saveRuleSet();
		} catch (IOException e) {
			Log.fatal(this, "Failed to save updated rule set", e);
		}
	}

	private int evaluateUnit(Map<String, Unit> units, File rootDir, File file) {
		Unit unit = new Unit();
		unit.modelFilename = file.getName();
		unit.modelRoot = "";
		unit.modelPackage = "";
		if (rootDir != null) {
			unit.modelRoot = rootDir.getName();
			unit.modelPackage = config.packageOf(file.getParent(), rootDir.getPath());
		}
		if (config.isExcludedPackage(unit.modelPackage)) {
			Log.debug(this, "Excluded package file: " + unit.modelFilename);
			return 0;
		}

		config.basePackage(unit.modelPackage);
		config.findGrammarName(unit.modelFilename);

		unit.unitName = FilenameUtils.getBaseName(unit.modelFilename);
		if (unit.unitName.length() == 0) {
			unit.unitName = FilenameUtils.getExtension(unit.modelFilename);
			unit.unitName = Strings.initialUC(unit.unitName);
		}
		if (units.containsKey(unit.unitName)) {
			Unit eUnit = units.get(unit.unitName);
			if (eUnit.modelFilename.equals(unit.modelFilename)
					&& eUnit.modelPackage.equals(unit.modelPackage)
					&& eUnit.modelRoot.equals(unit.modelRoot)) {
				// mark as existing physical file
				eUnit.primary = true;
				return 1;
			}
			unit.unitName += "1";
		}
		unit.primary = true;
		unit.license = null;
		unit.unitType = TypeOf.util;
		unit.templateVars = new ArrayList<String>();
		unit.parts = new ArrayList<String>();
		unit.templateGroup = "Unknown";
		Log.info(this, "Adding: " + Strings.concat(unit.modelRoot, unit.modelPackage, unit.modelFilename));
		units.put(unit.unitName, unit);
		return 1;
	}

	private boolean processModelToTemplates(Map<String, Unit> units) {
		Log.debug(this, "Prep templates directory");
		try {
			tmpDir = Utils.createTmpDir();
			tmpDir.deleteOnExit();
		} catch (IOException e) {
			Log.error(this, "Failed to create temp dir", e);
			return false;
		}

		for (String uname : units.keySet()) {
			Unit unit = units.get(uname);
			switch (unit.unitType) {
				case ignore:
				case binary:
				case document:
					continue;
				default:
					try {
						Template tmpl = fetchTemplateGroup(unit);
						Map<String, String> contents = processUnit(unit, tmpl);
						storeUnitContent(unit, tmpl, contents);
					} catch (IOException | IllegalArgumentException e) {
						Log.error(this, "Failure in processing umit: " + unit.unitName, e);
						return false;
					}
			}
		}
		return true;
	}

	private Map<String, String> processUnit(Unit unit, Template tmpl) throws IOException {
		unit.m_pathname = config.getUnitPathname(unit);
		Log.debug(this, "Accessing: " + unit.m_pathname);

		Map<String, String> fragments = fetchFragments(unit, tmpl);
		boolean natural = fragments.get(CONTENT).length() > 0 && unit.templateVars.size() > 0;
		if (natural || unit.literal) {
			String content = fragments.get(CONTENT);

			// add a level of excapes to existing escapes; will be reversed by ST
			content = content.replace("\\", "\\\\");
			// escape close braces to prevent interference with anon templates
			content = content.replace("}", "\\}");
			// escape native delimiter characters
			content = content.replace(tmpl.lDelim, "\\" + tmpl.lDelim);
			if (!tmpl.lDelim.equals(tmpl.rDelim)) {
				content = content.replace(tmpl.rDelim, "\\" + tmpl.rDelim);
			}

			if (unit.literal) {
				content += tmpl.lDelim + unit.unitName + "(";
				content += Strings.toCsv(unit.templateVars);
				content += ")" + tmpl.rDelim;
			} else {
				// convert key text to template var names
				for (String var : unit.templateVars) {
					Variable vSpec = config.getVariable(var);
					vSpec.varText = tmpl.lDelim + vSpec.variable + tmpl.rDelim;

					if (vSpec.replace != null) {
						vSpec.replacement = vSpec.replace.replace(vSpec.marker, vSpec.varText);
					} else {
						vSpec.replacement = vSpec.varText;
					}

					String search = vSpec.search.replace("\\", "\\\\");
					switch (vSpec.vType) {
						case MatchVar:
						case MatchVarList:
							content = content.replace(search, vSpec.replacement);
							break;
						case RegexVar:
						case RegexVarList:
							content = content.replaceAll(search, vSpec.replacement);
							break;
					}

					// %nameList:
					// {name|import %packageName%.parser.gen.%modelGrammar%Parser.%name%Context; }%
					switch (vSpec.vType) {
						case MatchVarList:
						case RegexVarList:
							String listPrefix = tmpl.lDelim + vSpec.variable + "List:{ " + vSpec.variable + " | ";
							String listSuffix = " }" + tmpl.rDelim;
							content = listPrefix + content + listSuffix;
							break;
						default:
					}
				}
			}
			fragments.put(CONTENT, content);
		}
		return fragments;
	}

	private void storeUnitContent(Unit unit, Template tmpl, Map<String, String> contents) throws IOException {
		boolean seconds = writeSecondaryContent(unit, tmpl, contents);
		seconds |= unit.license != null;

		String vars = Strings.toCsv(unit.templateVars);
		writeBlankLine(tmpl.tmplFile);
		String uname = unit.unitName + (unit.literal ? "Literal" : "");
		writeLine(tmpl.tmplFile, uname + "(" + vars + ") ::= <<");
		if (seconds) {
			writeLine(tmpl.tmplFile, tmpl.lDelim + "hdrBeg()" + tmpl.rDelim);
			if (unit.copyright && contents.get(COPYRIGHT) != null) {
				writeLine(tmpl.tmplFile, tmpl.lDelim + unit.unitName + COPYRIGHT + "()" + tmpl.rDelim);
			}
			if (unit.license != null) {
				writeLine(tmpl.tmplFile, tmpl.lDelim + unit.license + "()" + tmpl.rDelim);
			}
			if (unit.contributor && contents.get(CONTRIBUTOR) != null) {
				writeLine(tmpl.tmplFile, tmpl.lDelim + "hdrBlankLn()" + tmpl.rDelim);
				writeLine(tmpl.tmplFile, tmpl.lDelim + "hdrContribPrefix()" + tmpl.rDelim);
				writeLine(tmpl.tmplFile, tmpl.lDelim + unit.unitName + CONTRIBUTOR + "()" + tmpl.rDelim);
			}
			if (unit.description && contents.get(DESCRIPTION) != null) {
				writeLine(tmpl.tmplFile, tmpl.lDelim + "hdrBlankLn()" + tmpl.rDelim);
				writeLine(tmpl.tmplFile, tmpl.lDelim + unit.unitName + DESCRIPTION + "()" + tmpl.rDelim);
			}
			if (unit.version && contents.get(VERSION) != null) {
				writeLine(tmpl.tmplFile, tmpl.lDelim + "hdrBlankLn()" + tmpl.rDelim);
				writeLine(tmpl.tmplFile, tmpl.lDelim + "hdrVersionPrefix()" + tmpl.rDelim);
				writeLine(tmpl.tmplFile, tmpl.lDelim + unit.unitName + VERSION + "()" + tmpl.rDelim);
				writeLine(tmpl.tmplFile, tmpl.lDelim + "hdrBlankLn()" + tmpl.rDelim);
			}
			writeLine(tmpl.tmplFile, tmpl.lDelim + "hdrEnd()" + tmpl.rDelim);
		}

		writeLine(tmpl.tmplFile, contents.get(CONTENT));
		writeLine(tmpl.tmplFile, ">>");
	}

	private boolean writeSecondaryContent(Unit unit, Template tmpl, Map<String, String> contents) throws IOException {
		boolean second = false;
		if (unit.copyright && contents.get(COPYRIGHT) != null) {
			writeFragment(unit.unitName, COPYRIGHT, tmpl, contents.get(COPYRIGHT));
			second = true;
		}
		if (unit.contributor && contents.get(CONTRIBUTOR) != null) {
			writeFragment(unit.unitName, CONTRIBUTOR, tmpl, contents.get(CONTRIBUTOR));
			second = true;
		}
		if (unit.description && contents.get(DESCRIPTION) != null) {
			writeFragment(unit.unitName, DESCRIPTION, tmpl, contents.get(DESCRIPTION));
			second = true;
		}
		if (unit.version && contents.get(VERSION) != null) {
			writeFragment(unit.unitName, VERSION, tmpl, contents.get(VERSION));
			second = true;
		}
		return second;
	}

	private void writeFragment(String base, String name, Template tmpl, String content) throws IOException {
		writeBlankLine(tmpl.tmplFile);
		writeLine(tmpl.tmplFile, base + name + "() ::= <<");
		writeLine(tmpl.tmplFile, content);
		writeLine(tmpl.tmplFile, ">>");
	}

	private Map<String, String> fetchFragments(Unit unit, Template tmpl) throws IOException {
		File file = new File(unit.m_pathname);
		if (!file.exists() || !file.isFile() || !file.canRead()) {
			throw new IOException("Unable to access file: " + unit.m_pathname);
		}
		String content = FileUtils.readFileToString(file);
		if (content.length() == 0) {
			throw new IOException("File is empty: " + unit.m_pathname);
		}

		Map<String, String> fragments = new HashMap<>();
		if (tmpl.divider == null) {
			fragments.put(CONTENT, content);
		} else {
			String divider = tmpl.divider.replace(tmpl.marker, unit.unitName);
			String contentPart = split(content, divider, false);
			fragments.put(CONTENT, contentPart);
			if (unit.copyright) {
				divider = tmpl.divider.replace(tmpl.marker, COPYRIGHT);
				String cpr = " " + split(content, divider, true);
				fragments.put(COPYRIGHT, cpr);
			}
			if (unit.contributor) {
				divider = tmpl.divider.replace(tmpl.marker, CONTRIBUTOR);
				String ctbr = " " + split(content, divider, true);
				fragments.put(CONTRIBUTOR, ctbr);
			}
			if (unit.description) {
				divider = tmpl.divider.replace(tmpl.marker, DESCRIPTION);
				String desc = " " + split(content, divider, true);
				fragments.put(DESCRIPTION, desc);
			}
			if (unit.version) {
				divider = tmpl.divider.replace(tmpl.marker, VERSION);
				String ver = " " + split(content, divider, true);
				fragments.put(VERSION, ver);
			}
		}
		return fragments;
	}

	private String split(String content, String divider, boolean empty) {
		String[] parts = content.split(divider);
		switch (parts.length) {
			case 1:
				Log.warn(this, "Found 1 part - no splits");
				if (empty) return null;
				return Strings.trimLead(parts[0]);
			case 2:
				Log.debug(this, "Found 2 parts - leading empty string in first part; choosing second");
				return Strings.trimLead(parts[1]);
			case 3:
				Log.trace(this, "Found 3 parts - standard split; middle part is fragment");
				return Strings.trimLead(parts[1]);
			default:
				Log.error(this, "Found " + parts.length + " parts; obvious error, returning original content");
				return Strings.trimLead(content);
		}
	}

	/*
	 * Retrieve template spec referenced by the given unit. Create the group file, initialize it, &
	 * keep File reference in the template spec.
	 */
	private Template fetchTemplateGroup(Unit unit) throws IOException, IllegalArgumentException {
		String group = unit.templateGroup;
		if (group == null || group.length() == 0) {
			throw new IllegalArgumentException("Blank template group \"" + group + "\" for unit " + unit.unitName);
		}
		Template tmpl = config.getTemplate(group);
		if (tmpl == null) {
			throw new IllegalArgumentException("Template spec \"" + group + "\" not found for unit " + unit.unitName);
		}
		if (tmpl.tmplFile == null) {
			tmpl.pathname = config.getTemplatePathname(tmpl.name);
			tmpl.tmpPathname = config.getTmpTmplPathName(tmpDir, tmpl.name);
			Log.info(this, "Initializing: " + tmpl.tmpPathname);
			tmpl.tmplFile = new File(tmpl.tmpPathname);

			if (tmpl.delimiter != null) writeStgDelimiterStmt(tmpl);
			if (tmpl.imports != null) writeStgImportsStmt(tmpl);
		}
		return tmpl;
	}

	private void writeStgDelimiterStmt(Template tmpl) throws IOException {
		switch (tmpl.delimiter.length()) {
			case 1:
				tmpl.lDelim = tmpl.delimiter;
				tmpl.rDelim = tmpl.delimiter;
				break;
			case 2:
				tmpl.lDelim = tmpl.delimiter.substring(0, 1);
				tmpl.rDelim = tmpl.delimiter.substring(1);
				break;
		}
		writeLine(tmpl.tmplFile, "delimiters \"" + tmpl.lDelim + "\", \"" + tmpl.rDelim + "\"");
		Log.debug(this, "Delimiters: " + tmpl.delimiter + " ==> " + tmpl.lDelim + ", " + tmpl.rDelim);

	}

	private void writeStgImportsStmt(Template tmpl) throws IOException {
		for (String name : tmpl.imports) {
			name = FilenameUtils.concat(config.getTemplatePath(), name);
			name = Strings.setExtension(name, ConfigBase.templateGroupExt);
			writeLine(tmpl.tmplFile, "import \"" + name + "\"");
			Log.debug(this, "Importing: " + name);
		}
	}

	private void writeBlankLine(File file) throws IOException {
		writeLine(file, "");
	}

	private void writeLine(File file, String content) throws IOException {
		FileUtils.writeStringToFile(file, content + Strings.eol, true);
	}
}
