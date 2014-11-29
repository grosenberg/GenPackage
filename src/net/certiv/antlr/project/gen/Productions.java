package net.certiv.antlr.project.gen;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.certiv.antlr.project.base.CtxMethod;
import net.certiv.antlr.project.base.TypeOf;
import net.certiv.antlr.project.regen.spec.Unit;
import net.certiv.antlr.project.util.Log;
import net.certiv.antlr.project.util.Strings;
import net.certiv.antlr.project.util.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.atteo.evo.inflector.English;

public class Productions {

	private static final Pattern ctxClass = Pattern.compile("(public static class (\\w+) .*?\\}\\s+\\})",
			Pattern.DOTALL);
	private static final Pattern ctxMthds = Pattern.compile("public (\\w+)(\\<\\w+\\>)? (\\w+)(\\(.*?\\))?");

	private GenConfig config;
	private GenOptions opts;
	private SrcGenerator srcGen;

	// ///////////////////////////////////////////////////////////////////////////

	public Productions(GenOptions opts, GenConfig config) {
		super();
		this.opts = opts;
		this.config = config;
		this.srcGen = new SrcGenerator(config, opts.flagForce());
		init();
	}

	private void init() {
		config.loadRuleSet(config.getRuleSetPathname());
		if (config.isRuleSetLoaded() && generatedParserExists()) {
			parseGeneratedFiles();
		} else {
			Log.info(this, "A generated parser is required before descriptors can be generated");
		}
	}

	public boolean createDirs() {
		boolean ok = false;
		if (config.isRuleSetLoaded()) {
			List<String> pathList = new ArrayList<>();
			for (Unit unit : config.getUnits().values()) {
				String opath = config.getGenPathname(unit);
				opath = FilenameUtils.getFullPathNoEndSeparator(opath);
				// TODO: this is a hack
				if (opath.endsWith("parser")) {
					opath += Strings.pathSepStr + "gen";
				}
				addUniquePath(pathList, opath);
			}

			ok = !pathList.isEmpty();
			if (ok && opts.flagCreate()) {
				for (String path : pathList) {
					Log.debug(this, "Making: " + path);
					ok &= Utils.createDirs(path);
				}
			}
			// if (ok && opts.flagVerify()) {} // TODO: add verify option to gen?
		}
		return ok;
	}

	private void addUniquePath(List<String> paths, String path) {
		for (String p : paths) {
			if (p.startsWith(path)) return; // includes equal
			if (path.startsWith(p)) {
				int idx = paths.indexOf(p);
				paths.set(idx, path);
				return;
			}
		}
		paths.add(path);
	}

	public boolean generatedParserExists() {
		String pathname = config.getGenParserPathname();
		File fParser = new File(pathname);
		return fParser.exists();
	}

	private boolean parseGeneratedFiles() {
		if (!generatedParserExists()) {
			Log.error(this, "Generated parser does not exist!");
			return false;
		}

		File fParser = new File(config.getGenParserPathname());
		try {
			return parseParser(fParser);
		} catch (IOException e) {
			Log.error(this, "Failure in parsing the parser", e);
		}
		return false;
	}

	private boolean parseParser(File fParser) throws IOException {
		Map<String, List<CtxMethod>> ctxs = config.getContextMethods();
		Map<String, List<String>> importCtxs = config.getImportContexts();

		String parser = FileUtils.readFileToString(fParser.getCanonicalFile());
		Matcher m = ctxClass.matcher(parser);
		while (m.find()) {
			String ctx = m.group(1);
			String ctxName = m.group(2);
			if (ctx == null || ctxName == null) return false;

			ctxName = ctxName.replace("Context", "");

			List<CtxMethod> methods = new ArrayList<>();
			List<String> imports = new ArrayList<>();

			// import statements will always include self
			imports.add(ctxName);

			Matcher o = ctxMthds.matcher(ctx);
			while (o.find()) {
				CtxMethod ms = new CtxMethod();

				ms.retClass = o.group(1);
				ms.retType = o.group(2);
				ms.callName = o.group(3);
				ms.callType = o.group(4);

				// skip unwanted cases that bleed through the regexp
				if (ms.retClass.equals("int")) continue;
				if (ms.retClass.equals("static")) continue;
				if (ms.callType != null && ms.callType.length() > 2) continue;

				if (ms.retClass == null || ms.callName == null) return false;

				// process values
				// ms.presName = Strings.initialLC(ms.callName);
				ms.presName = ms.callName;
				if (ms.callType == null) ms.callType = "";
				if (ms.retType != null) {
					ms.list = true;
					ms.retClass += ms.retType;
					ms.retType = ms.retType.substring(1, ms.retType.length() - 1);
					ms.presName = English.plural(ms.presName);

					if (ms.retType.endsWith("Context")) {
						ms.contextName = ms.retType.substring(0, ms.retType.lastIndexOf("Context"));
						if (!imports.contains(ms.contextName)) {
							imports.add(ms.contextName);
						}
					}
				} else {
					if (ms.retClass.endsWith("Context")) {
						ms.contextName = ms.retClass.substring(0, ms.retClass.lastIndexOf("Context"));
						if (!imports.contains(ms.contextName)) {
							imports.add(ms.contextName);
						}
					}
				}
				methods.add(ms);
			}
			ctxs.put(ctxName, methods);
			importCtxs.put(ctxName, imports);
		}
		config.setContextMethods(ctxs);
		config.setImportContexts(importCtxs);
		return true;
	}

	// generate specialized files
	public boolean generateProject() {
		Log.info(this, "Beginning project generation run");

		List<TypeOf> types = new ArrayList<>();
		if (opts.flagCreate()) {
			types.addAll(Arrays.asList(TypeOf.values()));
			types.remove(TypeOf.ignore);
			if (!generatedParserExists()) {
				types.remove(TypeOf.descriptor);
				types.remove(TypeOf.derived);
				types.remove(TypeOf.walker);
			}

		} else if (opts.flagDescriptors()) {
			if (generatedParserExists()) types.add(TypeOf.descriptor);
		}

		Map<String, Unit> units = config.getRuleSet().units;
		for (String uname : units.keySet()) {
			Unit unit = units.get(uname);

			// Excusions
			if (!types.contains(unit.unitType)) continue;
			if (!unit.primary) continue;

			// Special case - resources copied from model to new project
			if (unit.unitType == TypeOf.binary || unit.unitType == TypeOf.document) {
				String srcbinary = Strings.concat(config.getModelBasePath(), unit.modelRoot, unit.modelFilename);
				String destdir = Strings.concat(config.getGenProjectPath(), unit.modelRoot);
				try {
					FileUtils.copyFileToDirectory(new File(srcbinary), new File(destdir));
				} catch (IOException e) {
					Log.error(this, "Binary element copy failed.", e);
				}
				continue;
			}

			// transform and render everything else
			srcGen.dispatch(unit);
		}
		Log.info(this, "Done");
		return true;
	}
}
