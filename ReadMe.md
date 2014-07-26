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
