package net.certiv.antlr.project.base;

public enum VarType {
	MatchVar,		// match the search string and replace with %variable%
	MatchVarList,	// same and handle call as list
	RegexVar,		// search string is a regex; replace with %variable%
	RegexVarList;	// same and handle call as list
}
