package net.certiv.antlr.project.gen;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.certiv.antlr.project.regen.spec.Unit;
import net.certiv.antlr.project.regen.spec.Variable;
import net.certiv.antlr.project.util.Log;
import net.certiv.antlr.project.util.Reflect;
import net.certiv.antlr.project.util.Strings;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class SrcGenerator {

	private GenConfig config;
	private List<String> builtins;
	private HashMap<String, String> aliases;
	private boolean overwrite;

	public SrcGenerator(GenConfig config, boolean overwrite) {
		super();
		this.config = config;
		this.overwrite = overwrite;
		init();
	}

	private void init() {
		aliases = new HashMap<String, String>();
		builtins = Reflect.getMethodNames(config, "get");
		for (String name : builtins) {
			aliases.putAll(permute(name));
		}
	}

	private Map<String, String> permute(String name) {
		HashMap<String, String> p = new HashMap<String, String>();
		String alias = name.substring(3);
		p.put(alias, name);
		alias = Strings.initialLC(alias);
		p.put(alias, name);
		if (name.startsWith("getGen")) {
			alias = name.substring(6);
			p.put(alias, name);
			alias = Strings.initialLC(alias);
			p.put(alias, name);
		}
		p.put(alias.toLowerCase(), name);
		return p;
	}

	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * Renders the given primary unit to the file identified by the unit
	 * 
	 * @param unit
	 *            the unit to render
	 */
	public void dispatch(Unit unit) {
		try {
			switch (unit.unitType) {
				case descriptor:
					for (String cName : config.getContextMethods().keySet()) {
						config.setCurrentContextName(cName);
						generate(unit, cName);
					}
					break;
				default:
					config.setCurrentContextName(null);
					generate(unit, null);
			}
		} catch (IOException e) {
			Log.error(this, "Problem generating unit: " + unit.unitName, e);
		}
	}

	private void generate(Unit unit, String cName) throws IOException {
		String out = config.getGenPathname(unit);
		Log.info(this, "Generating project file: " + out);
		StringBuilder content = new StringBuilder();
		generate(unit, content, cName);
		config.writeFile(out, content.toString(), overwrite);
	}

	/**
	 * Recursive content generator. Generates all parts in order starting with a given primary unit.
	 * 
	 * @param unit
	 *            the unit to render
	 * @param content
	 *            accumulator for rendered templates
	 * @throws IOException
	 */
	private void generate(Unit unit, StringBuilder content, String cName) throws IOException {
		String tmpl = config.getTemplatePathname(unit.templateGroup);
		Map<String, Object> varMap = new HashMap<>();
		for (String vname : unit.templateVars) {
			Variable var = config.getVariables().get(vname);
			String methodName = aliases.get(var.variable);
			if (methodName == null) throw new IOException("Unknown variable name: " + vname + " ==> " + var.variable);
			Object value = Reflect.invoke(true, config, methodName);
			if (value == null && cName != null) value = Reflect.invoke(true, config, methodName, cName);
			if (value == null) throw new IOException("Null variable value: " + vname + " ==> " + var.variable);
			varMap.put(vname, value);
		}
		content.append(create(tmpl, unit.unitName, varMap));

		if (unit.parts.size() == 0) return;
		// do not generate parts referenced from a literal unit
		if (unit.literal) return;
		for (String part : unit.parts) {
			Unit pUnit = config.getRuleSet().units.get(part);
			generate(pUnit, content, cName);
		}
	}

	/**
	 * Renders and returns the content from a template instance
	 * 
	 * @param t_pathname
	 *            template group name
	 * @param uname
	 *            template instance name
	 * @param varMap
	 *            map of template instance var names and values
	 * @return
	 * @throws IOException
	 */
	private String create(String t_pathname, String uname, Map<String, Object> varMap)
			throws IOException {

		boolean err = false;
		STGroup group = new STGroupFile(t_pathname);
		ST st = group.getInstanceOf(uname);
		if (st == null) {
			Log.error(this, "Failed to find template: " + uname);
			return "";
		}
		for (String varName : varMap.keySet()) {
			try {
				st.add(varName, varMap.get(varName));
			} catch (NullPointerException e) {
				Log.error(this, "Error adding attribute: " + uname + ":" + varName + " [" + e.getMessage() + "]");
				err = true;
			}
		}
		if (err) {
			Log.warn(this, "Skipping rendering of " + uname);
			return "";
		}
		return st.render();
	}
}
