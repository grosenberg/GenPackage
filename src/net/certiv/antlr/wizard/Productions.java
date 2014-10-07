package net.certiv.antlr.wizard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.certiv.antlr.wizard.util.Log;
import net.certiv.antlr.wizard.util.Strings;

public class Productions {

	private static final Pattern ruleName = Pattern.compile("void enter(\\w*)\\(");
	private static final Pattern ctxClass = Pattern.compile("(public static class (\\w+) .*?\\}\\s+\\})",
			Pattern.DOTALL);
	private static final Pattern ctxMthds = Pattern.compile("public (\\w+)(\\<\\w+\\>)? (\\w+)\\(\\)");

	private Config config;
	private CliOptions opts;
	private SrcGenerator srcGen;

	private List<String> genNames;
	private Map<String, List<CtxMethod>> ctxs;

	public static class Context {

		public String ctxName;
		public List<CtxMethod> ctxMethods;
	}

	public static class CtxMethod {

		public String retClass;
		public String retType;
		public String callName;
		public String presName;
	}

	// ///////////////////////////////////////////////////////////////////////////

	public Productions(CliOptions opts, Config config) {
		super();
		this.opts = opts;
		this.config = config;
		this.srcGen = new SrcGenerator(config);
	}

	public boolean validBuildConfiguration() {
		String msg = "Insufficient parameters to build project files";
		if (checkValues(msg, config.getProjectPath(), config.getSourcePath(), config.getPackageName(),
				config.getAntlrJarPathName(), config.getJavaPath(), config.getGrammarName())) {
			return true;
		}
		return false;
	}

	public boolean createDirectories() {
		if (config.isLoaded()) {
			if (opts.flagCreate()) {
				return createDirs(config.getDstBase())
						&& createDirs(config.getDstDescriptors())
						&& createDirs(config.getDstGen())
						&& createDirs(config.getDstGenerator())
						&& createDirs(config.getDstSymbol())
						&& createDirs(config.getDstTypes())
						&& createDirs(config.getDstUtil())
						&& createDirs(config.getDstTestBase());
			}
			return true;
		}
		return false;
	}

	public boolean createStandardTools() {
		try {
			srcGen.createTools(config.getProjectPath(), config.getSourcePath(), config.getPackagePath(),
					config.getAntlrJarPathName(), config.getJavaPath(), config.getGrammarName());
			srcGen.createUtils(config.getPackageName());
			srcGen.createLogConfig(config.getPackageName(), config.getGrammarName());
		} catch (IOException e) {
			Log.error(this, "Failed to create tools", e);
			return false;
		}
		return true;
	}

	public boolean createDefaultParser() {
		try {
			srcGen.createSymbols(config.getPackageName());
			srcGen.createTypes(config.getPackageName());
			srcGen.createParser(config.getPackageName(), config.getGrammarName());
		} catch (IOException e) {
			Log.error(this, "Failed to create default parser", e);
			return false;
		}
		return true;
	}

	public boolean generatedParserExists() {
		File fParser = new File(Strings.concat(config.getDstGen(), config.getGrammarName() + "Parser.java"));
		if (!fParser.exists()) {
			return false;
		}

		String lastError = "Unknown";
		try {
			lastError = "Failure in parsing the base listener";
			genNames = parseBaseListener(config.getGrammarName());

			lastError = "Failure in parsing the parser";
			ctxs = parseParser(fParser);
		} catch (IOException e) {
			Log.error(this, lastError, e);
			return false;
		}
		return true;
	}

	public boolean createStandardParserTools() {
		try {
			srcGen.createBaseClasses(config.getPackageName(), config.getGrammarName());
			srcGen.createWalkerPhase(config.getPackageName(), config.getGrammarName(), genNames);
			srcGen.createGenerator(config.getPackageName(), config.getGrammarName());
		} catch (IOException e) {
			Log.error(this, "Failure in basic classes creation", e);
			return false;
		}
		return true;
	}

	public boolean createBaseDescriptor() {
		try {
			srcGen.createDescriptorBasis(config.getPackageName(), config.getGrammarName(), genNames, ctxs);
		} catch (IOException e) {
			Log.error(this, "Failure creating base descriptor", e);
			return false;
		}
		return true;
	}

	public boolean createDescriptors() {
		try {
			srcGen.createDescriptors(config.getPackageName(), config.getGrammarName(), genNames, ctxs);
		} catch (IOException e) {
			Log.error(this, "Failure in creating descriptors", e);
			return false;
		}
		return true;
	}

	public boolean createStandardTests() {
		try {
			srcGen.createTests(config.getPackageName(), config.getGrammarName());
		} catch (IOException e) {
			Log.error(this, "Failure in basic classes creation", e);
			return false;
		}
		return true;
	}

	// ///////////////////////////////////////////////////////////////////////////

	List<String> parseBaseListener(String grammarName) throws IOException {
		List<String> genNames = new ArrayList<>();
		String baseListener = config.readFile(Strings.concat(config.getDstGen(), grammarName
				+ "ParserBaseListener.java"));
		Matcher m = ruleName.matcher(baseListener);
		while (m.find()) {
			String genName = m.group(1);
			if (!"EveryRule".equals(genName)) {
				genNames.add(genName);
			}
		}
		return genNames;
	}

	Map<String, List<CtxMethod>> parseParser(File fParser) throws IOException {
		Map<String, List<CtxMethod>> ctxs = new HashMap<>();
		String parser = config.readFile(fParser.getPath());
		Matcher m = ctxClass.matcher(parser);
		while (m.find()) {
			String ctx = m.group(1);
			String ctxName = m.group(2);

			Matcher o = ctxMthds.matcher(ctx);
			List<CtxMethod> methods = new ArrayList<>();
			while (o.find()) {
				CtxMethod ms = new CtxMethod();
				ms.retClass = o.group(1);
				ms.retType = o.group(2);
				ms.callName = o.group(3);
				if (o.group(2) != null) {
					ms.retClass += ms.retType;
					ms.retType = ms.retType.substring(1, ms.retType.length() - 1);
				}
				if (!ms.retClass.equals("int")) {
					// skip odd case that bleads through the regexp
					methods.add(ms);
				}
			}
			ctxs.put(ctxName, methods);
		}
		return ctxs;
	}

	boolean createDirs(String dir) {
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

	boolean checkValues(String reason, String... values) {
		for (String value : values) {
			if (value == null || value.length() == 0) {
				Log.warn(this, reason);
				return false;
			}
		}
		return true;
	}
}
