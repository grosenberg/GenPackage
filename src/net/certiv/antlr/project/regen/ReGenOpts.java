package net.certiv.antlr.project.regen;

import java.io.File;
import java.io.IOException;

import net.certiv.antlr.project.util.Log;
import net.certiv.antlr.project.util.Strings;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FilenameUtils;

public class ReGenOpts {

	private Options options;
	private CommandLine cmd;
	private String filepath;
	private String filename;

	public ReGenOpts() {
		options = new Options();
		options.addOption("c", "create", false, "create sample rules config file");
		options.addOption("d", "debug", false, "debug logging level");
		options.addOption("f", "force", false, "force overwrite operation (use with caution)");
		options.addOption("h", "help", false, "help: print usage information");
		options.addOption("q", "quiet", false, "only log warnings and errors");
		options.addOption("r", "rebuild", false, "rebuild the templates");
		options.addOption("u", "update", false, "update the configuration from the existing model");
		options.addOption("v", "verify", false, "verify: check the config for internal consistency");
	}

	public boolean processOptions(String[] args) {
		CommandLineParser parser = new PosixParser();
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			Log.error(this, "Command line parse failure: " + e.getMessage());
			return false;
		}
		return noUnrecognized();
	}

	private boolean noUnrecognized() {
		if (flagHelp()) return true;
		if (cmd.getArgs().length > 1) { // getArgs is really getUnrecognizedArgs!!!
			Log.error(this, "Unrecognized Options: " + cmd.getArgList().toString());
			return false;
		}
		if (cmd.getArgs().length == 1) {
			filepath = cmd.getArgs()[0];
			if (filepath.startsWith("-")) {
				Log.error(this, "Unrecognized Options: " + cmd.getArgList().toString());
				filepath = null;
				return false;
			}
		} else {
			filepath = ".";
		}
		if (filepath.equals(".")) {
			File f = new File(filepath);
			try {
				filepath = f.getCanonicalPath();
				if (f.exists() && f.isDirectory()) {
					if (!filepath.endsWith("" + Strings.pathSep)) {
						filepath += Strings.pathSep;
					}
				}
			} catch (IOException e) {
				Log.error(this, "Error parsing ruleset pathname.", e);
				return false;
			}
		}
		if (filepath.endsWith(".json")) {
			filename = FilenameUtils.getName(filepath);
			filepath = FilenameUtils.getFullPath(filepath);
		} else if (!filepath.endsWith("" + Strings.pathSep)) {
			filepath += Strings.pathSep;
		}
		return true;
	}

	public void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ReGen [options] config", options);
	}

	public int argCount() {
		return cmd.getOptions().length;
	}

	public boolean flagCreate() {
		return cmd.hasOption("c");
	}

	public boolean flagDebug() {
		return cmd.hasOption("d");
	}

	public boolean flagForce() {
		return cmd.hasOption("f");
	}

	public boolean flagHelp() {
		return cmd.hasOption("h");
	}

	public boolean flagQuiet() {
		return cmd.hasOption("q");
	}

	/**
	 * Rebuild is the default if no other constructive options (Create, Update, Verify) are
	 * specified.
	 * 
	 * @return
	 */
	public boolean flagRebuild() {
		return cmd.hasOption("r") || (!flagCreate() && !flagUpdate() && !flagVerify());
	}

	public boolean flagUpdate() {
		return cmd.hasOption("u");
	}

	public boolean flagVerify() {
		return cmd.hasOption("v");
	}

	public String filepath() {
		return filepath;
	}

	public String filename() {
		return filename;
	}
}
