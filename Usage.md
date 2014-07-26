### Antlr GenProject Wizard -- Antlr4 project generator
(c) 2012-2014 Gerald Rosenberg, Certiv Analytics

Antlr GenProject provides a command line wizard for generating 
an Antlr4 project framework with a fairly standard form.  The 
directory structure is compatible with standard Eclipse/Java 
projects. 

#### License
BSD/EPL License

### The generated project implements two basic patterns:

1. Descriptor pattern: a full set of descriptor classes are generated
automatically from the Antlr generated files.  The descriptor classes 
are type-specific to the parse tree context nodes defined by the project grammar.
The default instantiation of the descriptors uses a decoration pattern 
allowing simple access to the parse tree contexts and corresponding descriptors.

2. Converter pattern: a generated converter class implements a serial procedure
for lexing, parsing, and walking operations.  Multiple, sequential walking phases 
are supported with the passing forward of state between phases. A full symbol table
implementation is provided as part of the default state object.  As generated, 
phase 1 constructs and initializes the descriptor object set and phase 2 invokes 
a process method on each descriptor object. 


### Instructions (Windows):
1. Unzip in a directory of your choice.  
2. Edit the included 'Run.bat' file (_aka_ the project runner)
to correct the paths to suit your system. Run the jar file with 
either -h and -H for canonical help and sample command line hints.
3. Execute project runner to generate a default directory structure 
and initial file set, including default grammar files in the 
parser subdirectory.
4. Replace or edit the grammar files to implement the target grammar.
5. Locate and execute the generated 'XXXXTool.bat' (_aka_ the project\'s
Antlr tool runner).  This will generate the Antlr lexer, parser, and 
listener java files.
6. Execute the project runner again. The descriptor and other remaining
project files will be generated.

### Instructions (Linux)
1. Linux users do not require instructions!
2. Linux users can convert Windows batch-files to their chosen shell in their sleep.
3. Linux users are generous and help other Linux users (please submit your runners 
for inclusion)

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

### Dependencies (for the Wizard and for file generation)
Antlr4 [ANTLR 4.4 Complete]
Apache [Logging Services](http://logging.apache.org/log4j/1.2/)

### Dependencies (just for the Wizard)
Apache [Commons CLI](http://commons.apache.org/proper/commons-cli/)
Apache [Commons IO](http://commons.apache.org/proper/commons-io/)
Google [Gson](https://code.google.com/p/google-gson/)

#### Command line
|Data arguments:|
|==|==|==|
|-g grammarName |Grammar name prefix| Json|
|-n packageName |Package pathname| net.certiv.json|
|-a antlrJar |system path to the Antlr jar| D:/DevFiles/Java/Libs/Antlr/antlr-4.2-complete.jar|
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
