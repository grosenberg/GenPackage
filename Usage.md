The GenProject Wizard -- An Antlr v4 basic project generator.  
(c) Gerald Rosenberg, Certiv Analytics
BSD License.

The GenProject wizard generates Antlr v4 project using a fairly
straightfoward form. It is unlikely to be the best or even
preferred form.  It is simply the form that seems natural to me
(the project originated mostly to memorialize the form for my
own projects).

The generated project implements two basic patterns:

1. a set of descriptor objects is provided so that every
context instance has a unique, typed descriptor object instance.  
2. a serial sequence of parse tree walker phases, where
phase01 creates the descriptor objects and phases 02-xx are
intended to conveniently partition operations against the
parse tree - exactly how is up to you.

Instructions (Windows):

1. Unzip in a directory of your choice.  
2. Edit the 'Run.bat' to correct the paths to suit your system.  
Run the jar file with just either -h or -H for some help and hints  
3. Run 'Run.bat'.  This will generate a default directory structure 
and initial file set
4. Locate the 'XXXXTool.bat' (default is 'JsonTool.bat') and run it.  
This will generate generate the Antlr files needed to complete
project file generation.  
5. Run the 'Run.bat' again. The remaining files are generated.

You can run the project generator any number of times and it will
generate any missing files (at least those that it thinks are missing).
It will not overwrite existing files, unless the '-f' flag is provided.

The Antlr Tool generated XXXXParserBaseListener.java file (default is
'JsonParserBaseListener.java') is examined to identify additional files
to be generated.



|Data arguments:|
|==|==|==|
|-n packageName |Package name to create| net.certiv.json|
|-g grammarName |Grammar name to create| Json|
|-p projectPath |system path to project directory| D:/DevFiles/Java/WorkSpaces/Main/MyJsonProject|
|-s sourcePath |internal path to the project source directory||
|-j javaPath |system path to java root directory|  C:/Program Files/Java/jre7/bin|
|-a antlrJar |system path to the antlr jar| D:/DevFiles/Java/Libs/Antlr/antlr-4.2-complete.jar|

|Procedurals:|
|==|==|
|-c |create project files|
|-t |create tools and basic grammar files|
|-d |create descriptor objects and related files|
|-h |help: print usage information|
|-H |Hint: print example usage information|
|-f |Force overwrite operation (use with caution)|
