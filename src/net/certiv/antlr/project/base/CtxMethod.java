package net.certiv.antlr.project.base;

public class CtxMethod {

	public Boolean list;		// true iff a list method

	public String retClass;		// class of value returned
	public String retType;		// type of value returned; non-null iff class is a container class
	public String callName; 	// method or field name
	public String callType;		// "()" iff callName is a method

	public String presName; 	// derivative of the callName
	public String contextName;	// derived name of current context
}
