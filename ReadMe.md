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

