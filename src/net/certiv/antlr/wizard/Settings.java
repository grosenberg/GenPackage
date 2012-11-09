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

import com.google.gson.annotations.Since;

/**
 * Minimal class used to marshal settings to and from a json file.
 * 
 * @author Gbr
 * 
 */
public class Settings {

	@Since(1.0)
	public String magicId = "<not set>"; // not null
	@Since(1.0)
	public boolean loaded = false; // pendantic

	@Since(1.0)
	public String grammarName; // "Metal"
	@Since(1.0)
	public String packageName; // "net.certiv.metal"
	@Since(1.0)
	public String packagePath; // derived from packageName: "net/certiv/metal"

	@Since(1.0)
	public String projectPath; // "D:/DevFiles/Java/WorkSpaces/Main/net.certiv.metal"
	@Since(1.0)
	public String sourcePath; // "src"
	@Since(1.0)
	public String javaPath; // system path to the jre/jdk bin directory
	@Since(1.0)
	public String antlrJarPathName; // system path and jar name

}
