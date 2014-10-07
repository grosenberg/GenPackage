package net.certiv.antlr.wizard;

import net.certiv.antlr.wizard.util.Strings;

public abstract class AbstractDstPaths {

	// derived values =========

	private String dstSource;
	private String dstBase;
	private String dstConverter;
	private String dstDescriptors;
	private String dstGenerator;
	private String dstParser;
	private String dstGen;
	private String dstSymbol;
	private String dstTypes;
	private String dstUtil;

	private String dstTest;
	private String dstTestBase;

	public abstract String getProjectPath();

	public abstract String getSourcePath();

	public abstract String getTestPath();

	public abstract String getPackagePath();

	void deriveDstPaths() {
		dstSource = Strings.concat(getProjectPath(), getSourcePath());
		dstBase = Strings.concat(dstSource, getPackagePath());
		dstConverter = Strings.concat(dstBase, "converter");
		dstDescriptors = Strings.concat(dstBase, "converter", "descriptors");
		dstParser = Strings.concat(dstBase, "parser");
		dstGen = Strings.concat(dstBase, "parser", "gen");
		dstGenerator = Strings.concat(dstBase, "generator");
		dstSymbol = Strings.concat(dstBase, "symbol");
		dstTypes = Strings.concat(dstBase, "types");
		dstUtil = Strings.concat(dstBase, "util");

		dstTest = Strings.concat(getProjectPath(), getTestPath(), getPackagePath(), "test");
		dstTestBase = Strings.concat(dstTest, "base");
	}

	public String getDstSource() {
		return dstSource;
	}

	public String getDstBase() {
		return dstBase;
	}

	public String getDstConverter() {
		return dstConverter;
	}

	public String getDstDescriptors() {
		return dstDescriptors;
	}

	public String getDstGenerator() {
		return dstGenerator;
	}

	public String getDstParser() {
		return dstParser;
	}

	public String getDstGen() {
		return dstGen;
	}

	public String getDstSymbol() {
		return dstSymbol;
	}

	public String getDstTypes() {
		return dstTypes;
	}

	public String getDstUtil() {
		return dstUtil;
	}

	public String getDstTest() {
		return dstTest;
	}

	public String getDstTestBase() {
		return dstTestBase;
	}
}
