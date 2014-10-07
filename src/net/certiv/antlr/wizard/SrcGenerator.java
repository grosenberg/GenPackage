package net.certiv.antlr.wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.certiv.antlr.wizard.Productions.CtxMethod;
import net.certiv.antlr.wizard.util.Strings;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class SrcGenerator {

	private static final String templateDir = "net/certiv/antlr/wizard/templates";
	private Config config;

	public SrcGenerator(Config config) {
		super();
		this.config = config;
	}

	// ///////////////////////////////////////////////////////////////////////////

	public void createWalkerPhase(String packageName, String grammarName, List<String> genNames) throws IOException {
		createFirstWalkerPhase(packageName, grammarName, "01");
		createWalkerPhase(packageName, grammarName, "02", genNames);
		createWalkerPhase(packageName, grammarName, "03", genNames);
		createPhaseState(packageName);
	}

	private void createFirstWalkerPhase(String packageName, String grammarName, String phaseNumber) throws IOException {
		STGroup group = new STGroupFile(Strings.concatAsClassPath(templateDir, "PhaseClasses.stg"));

		// use grammar name to define presumptive first rule name
		String genName = grammarName.substring(0, 1).toUpperCase();
		genName += grammarName.substring(1).toLowerCase();

		ST st = group.getInstanceOf("FirstPhaseClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		st.add("genName", genName);
		st.add("phaseNumber", phaseNumber);
		String result = st.render();
		config.writeFile(Strings.concat(config.getDstConverter(), grammarName + "Phase" + phaseNumber + ".java"),
				result);
	}

	private void createWalkerPhase(String packageName, String grammarName, String phaseNumber, List<String> genNames)
			throws IOException {
		STGroup group = new STGroupFile(Strings.concatAsClassPath(templateDir, "PhaseClasses.stg"));
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

		config.writeFile(Strings.concat(config.getDstConverter(), grammarName + "Phase" + phaseNumber + ".java"),
				result.toString());
	}

	private void createPhaseState(String packageName) throws IOException {
		STGroup group = new STGroupFile(Strings.concatAsClassPath(templateDir, "PhaseClasses.stg"));
		ST st = group.getInstanceOf("PhaseStateClass");
		st.add("packageName", packageName);
		String result = st.render();
		config.writeFile(Strings.concat(config.getDstConverter(), "PhaseState.java"), result);
	}

	public void createBaseClasses(String packageName, String grammarName) throws IOException {

		STGroup group = new STGroupFile(Strings.concatAsClassPath(templateDir, "BaseClasses.stg"));
		ST st = group.getInstanceOf("ConverterClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		st.add("startRule", grammarName.toLowerCase());
		String result = st.render();
		config.writeFile(Strings.concat(config.getDstConverter(), "Converter.java"), result);

		st = group.getInstanceOf("PhaseBaseClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstConverter(), grammarName + "PhaseBase.java"), result);
	}

	public void createParser(String packageName, String grammarName) throws IOException {
		STGroup group = new STGroupFile(Strings.concatAsClassPath(templateDir, "Parser.stg"));
		ST st = group.getInstanceOf("ParserGrammar");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		st.add("startRule", grammarName.toLowerCase());
		String result = st.render();
		config.writeFile(Strings.concat(config.getDstParser(), grammarName + "Parser.g4"), result);

		st = group.getInstanceOf("LexerGrammar");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstParser(), grammarName + "Lexer.g4"), result);

		st = group.getInstanceOf("LexerAdapterClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstParser(), "LexerAdaptor.java"), result);

		st = group.getInstanceOf("LexerHelperClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstParser(), "LexerHelper.java"), result);

		st = group.getInstanceOf("ErrorListenerClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstParser(), grammarName + "ErrorListener.java"), result);

		st = group.getInstanceOf("LexerErrorStrategyClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstParser(), grammarName + "LexerErrorStrategy.java"), result);

		st = group.getInstanceOf("ParserErrorStrategyClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstParser(), grammarName + "ParserErrorStrategy.java"), result);

		st = group.getInstanceOf("TokenClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstParser(), grammarName + "Token.java"), result);

		st = group.getInstanceOf("TokenFactoryClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstParser(), grammarName + "TokenFactory.java"), result);

	}

	public void createGenerator(String packageName, String grammarName) throws IOException {
		STGroup group = new STGroupFile(Strings.concatAsClassPath(templateDir, "FileGen.stg"));

		ST st = group.getInstanceOf("OutputFileGen");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		String result = st.render();
		config.writeFile(Strings.concat(config.getDstGenerator(), grammarName + "FileGen.java"), result);

		st = group.getInstanceOf("IOProcessorClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstGenerator(), "IOProcessor.java"), result);
	}

	public void createDescriptorBasis(String packageName, String grammarName, List<String> descriptorNames,
			Map<String, List<CtxMethod>> ctxs)
			throws IOException {
		STGroup group = new STGroupFile(Strings.concatAsClassPath(templateDir, "DescriptorClasses.stg"));
		ST st = group.getInstanceOf("IDescriptorClass");
		st.add("packageName", packageName);
		String result = st.render();
		config.writeFile(Strings.concat(config.getDstConverter(), "IDescriptor.java"), result);

		st = group.getInstanceOf("BaseDescriptorClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstConverter(), "BaseDescriptor.java"), result);

		st = group.getInstanceOf("ValueClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstConverter(), "Value.java"), result);
	}

	public void createDescriptors(String packageName, String grammarName, List<String> descriptorNames,
			Map<String, List<CtxMethod>> ctxs)
			throws IOException {
		STGroup group = new STGroupFile(Strings.concatAsClassPath(templateDir, "DescriptorClasses.stg"));
		ST st = group.getInstanceOf("DescriptorClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		for (String genName : descriptorNames) {
			List<CtxMethod> mthSet = ctxs.get(genName + "Context");
			updateMethodNames(mthSet);
			List<String> impNames = filterImports(mthSet);

			st.add("genName", genName);
			st.add("methods", mthSet);
			st.add("imports", impNames);
			String result = st.render();
			config.writeFile(Strings.concat(config.getDstDescriptors(), genName + "Descriptor.java"), result);
			st.remove("genName");
			st.remove("methods");
			st.remove("imports");
		}
	}

	private void updateMethodNames(List<CtxMethod> mthSet) {
		for (CtxMethod m : mthSet) {
			m.presName = Strings.tokenCase(m.callName);
		}
	}

	private List<String> filterImports(List<CtxMethod> mthSet) {
		ArrayList<String> impNames = new ArrayList<>();
		for (CtxMethod m : mthSet) {
			if (!(m.retClass.equals("TerminalNode") || (m.retType != null && m.retType.equals("TerminalNode")))) {
				if (m.retType != null) {
					impNames.add(m.retType);
				} else {
					impNames.add(m.retClass);
				}
			}
		}
		return impNames;
	}

	public void createTools(String projectPath, String sourcePath, String packagePath, String antlrJarPath,
			String javaPath, String grammarName) throws IOException {
		STGroup group = new STGroupFile(Strings.concatAsClassPath(templateDir, "Tools.stg"));
		ST st = group.getInstanceOf("ToolBatchFile");
		st.add("projectPath", projectPath);
		st.add("sourcePath", sourcePath);
		st.add("packagePath", packagePath);
		st.add("antlrJarPath", antlrJarPath);
		st.add("javaPath", javaPath);
		st.add("grammarName", grammarName);
		String result = st.render();
		config.writeFile(Strings.concat(config.getDstParser(), grammarName + "Tool.bat"), result);
	}

	public void createSymbols(String packageName) throws IOException {
		STGroup group = new STGroupFile(Strings.concatAsClassPath(templateDir, "Symbol.stg"));
		ST st = group.getInstanceOf("SymbolClass");
		st.add("packageName", packageName);
		String result = st.render();
		config.writeFile(Strings.concat(config.getDstSymbol(), "Symbol.java"), result);

		st = group.getInstanceOf("ScopeClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstSymbol(), "Scope.java"), result);

		st = group.getInstanceOf("SymbolTableClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstSymbol(), "SymbolTable.java"), result);
	}

	public void createTypes(String packageName) throws IOException {
		STGroup group = new STGroupFile(Strings.concatAsClassPath(templateDir, "Types.stg"));
		ST st = group.getInstanceOf("ValueTypeClass");
		st.add("packageName", packageName);
		String result = st.render();
		config.writeFile(Strings.concat(config.getDstTypes(), "ValueType.java"), result);

		st = group.getInstanceOf("StyleTypeClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstTypes(), "StyleType.java"), result);

		st = group.getInstanceOf("ContentClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstTypes(), "Content.java"), result);

		st = group.getInstanceOf("ScopeTypeClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstTypes(), "ScopeType.java"), result);

		st = group.getInstanceOf("StmtTypeClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstTypes(), "StmtType.java"), result);

		st = group.getInstanceOf("OpClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstTypes(), "Op.java"), result);

	}

	public void createUtils(String packageName) throws IOException {
		STGroup group = new STGroupFile(Strings.concatAsClassPath(templateDir, "Utils.stg"));
		ST st = group.getInstanceOf("LogClass");
		st.add("packageName", packageName);
		String result = st.render();
		config.writeFile(Strings.concat(config.getDstUtil(), "Log.java"), result);

		st = group.getInstanceOf("ReflectClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstUtil(), "Reflect.java"), result);

		st = group.getInstanceOf("StringsClass");
		st.add("packageName", packageName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstUtil(), "Strings.java"), result);
	}

	public void createLogConfig(String packageName, String grammarName) throws IOException {
		STGroup group = new STGroupFile(Strings.concatAsClassPath(templateDir, "Log.stg"));

		ST st = group.getInstanceOf("Log4J2File");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		String result = st.render();
		config.writeFile(Strings.concat(config.getDstSource(), "log4j2.xml"), result);
	}

	public void createTests(String packageName, String grammarName) throws IOException {
		STGroup group = new STGroupFile(Strings.concatAsClassPath(templateDir, "Tests.stg"));

		ST st = group.getInstanceOf("TestExampleClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		String result = st.render();
		config.writeFile(Strings.concat(config.getDstTest(), "Test" + grammarName + "Example.java"), result);

		st = group.getInstanceOf("AbstractBaseClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstTestBase(), "AbstractBase.java"), result);

		st = group.getInstanceOf("TestBaseClass");
		st.add("packageName", packageName);
		st.add("grammarName", grammarName);
		result = st.render();
		config.writeFile(Strings.concat(config.getDstTestBase(), "TestBase.java"), result);
	}
}
