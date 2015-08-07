### Antlr GenProject Wizard -- Antlr4 project generator
(c) 2012-2015 Gerald Rosenberg, Certiv Analytics

### Instructions (Windows):

1. Unzip or clone in to a directory of your choice.  
	1. Expected directory structure includes a 'jars' directory to contain the 
	GenProject jar and a 'libs' directory containing the project dependency jars.
	2. Build the project to create a jar file.
	3. For Eclipse users, use the 'GenProject-2.2-complete.xml' jar export 
	wizard description file to generate the GenProject jar.
2. Customize for local use
	1. Edit the 'InitProject.bat' file (_aka_ the project runner)
	to correct the directory paths to match your system. Also, update
	the project, package, and grammar names for the project to be created.
	2. Edit the GenProjectRuleSet.json file to correct absolute 
	pathnames.  This includes the base values `projectBasePath` and `modelBasePath`
	and any paths in the `variables` section.
3. New project initialization
	1. Execute the project runner to generate a project specific configuration file
	and 'GenProject.bat' file (_aka_ the project generator) in a target project
	directory.  
	2. For Eclipse users, the target directory will be initialized with the
	necessary dot files to be recognized as a standard Eclipse project directory.
4. New project generation (first run)
	1. Switch to the target project directory and edit the new files to correct 
	path and project values if and as desired.
	2. Execute the project generator to create the initial full project file set.  
	This will be an adapted version of the GenProjectModel file set with default 
	grammar files in the parser subdirectory.
5. Customize the grammar
	1. Switch to the parser directory.
	2. Replace or edit the grammar files to implement your intended target project grammar.
	The Antlr generated files will be placed in to the 'parser/gen' directory.
	3. Edit the 'XXXXTool.bat' file (_aka_ the Antlr tool runner) to fix path and 
	project names.  Make sure to list all grammar files to process.
	4. Execute the Antlr tool runner.  This will generate the Antlr lexer, parser, and 
	listener java files. Even if visitors are going to be used, be sure that the listener 
	files are also generated.
6. New project generation (second run)
	1. Execute the project runner again. The descriptor and other remaining
	project files will be generated.  Existing files will _not_ be overwritten. Missing
	files will be regenerated.
7. Project updates
	1. When desired, edit the project generator and change the command line parameter 
	'-c' to '-d'.  This will limit the project generator to only checking and creating 
	missing descriptor class files.
	2. Run the Antlr tool runner and project generator whenever desired to reflect grammar changes.

### Instructions (Linux)

1. Linux users do not require instructions!
2. Linux users can convert Windows batch-files to their chosen shell in their sleep.
3. If you wish to contribute a set of shell scripts, please submit a pull request.

### Notes

1. The Antlr tool runner can be executed at any time to rebuild the lexer,
parser, and listener java files.
2. The wizard requires the parser and listener java files in order to (re)generate
the descriptor java files.
3. The project runner can be executed at any time to regenerate files. 
4. The '-c' flag will regenerate all missing files.
5. The '-d' flag will limit regeneration to just missing descriptor files. 
6. The '-f' flag will force *all* files to be regenerated. It will overwrite 
existing files. Use with caution.
7. Orphaned descriptor files have to be removed manually.

### Dependencies
 
Antlr4 [ANTLR 4.5+ Complete](http://www.antlr.org/download.html)
Apache [Log4j2 Core and API](http://logging.apache.org/log4j/2.x/) 
Apache [Commons CLI](http://commons.apache.org/proper/commons-cli/)
Apache [Commons IO](http://commons.apache.org/proper/commons-io/)
Apache [Commons Lang](http://commons.apache.org/proper/commons-lang/)
Google [Gson](https://code.google.com/p/google-gson/)
Evo [Inflector](http://mvnrepository.com/artifact/org.atteo/evo-inflector/1.2.1)

#### Command line

|Data arguments:|
|==|==|==|
|-g grammarName |Grammar name prefix| Json|
|-n packageName |Package pathname| net.certiv.json|
|-a antlrJar |system path to the Antlr jar| D:/DevFiles/Java/Libs/Antlr/antlr-4.5-complete.jar|
|-j javaPath |system path to Java home directory|  C:/Program Files/Java/jre7/bin|
|-p projectPath |system path to project directory| D:/DevFiles/Java/WorkSpaces/Main/MyJsonProject|
|-s sourcePath |internal path to the project source directory|src|

|Procedurals:|
|==|==|
|-c |create all project files|
|-d |create descriptor files|
|-f |Force overwrite operation (use with caution)|
|-t |create tools and basic grammar files|
|-h |help: print usage information|
|-H |Hint: print example usage information|
