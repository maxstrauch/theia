# ![Theia program icon](/icon.png?raw=true "Theia program icon") Theia

[![Screenshot of the program](/screenshot_small.png?raw=true "Screenshot of the main application with faculty program and compiled bytecode view")](/screenshot.png?raw=true).

The Theia download can be found [here (with additional pieces of information)](#download) or [directly downloaded here](/dist/theia.jar?raw=true).

In my current theorectial computer science course we are talking about register machines. A register machine consists of registers `x1 ... xk` and executes statements which change the numerical values of this registers. They are "programmed" by the languages LOOP, WHILE and GOTO which have different features. Due to the fact that I really like designing programming languages and like dealing with parsers, lexers and acceptors I wrote a litte application which executes those programs. 

The mandatory faculty example might look like (in the `LOOP` language):

	x3 := x1 ;
	x2 := 1 ;
	loop x1 do
		x2 := x2 * x3 ;
		x3 := x3 - 1
	end
	x3 := 0

where register `x1` contains the n to calculate the faculty for.

Starting from the source code Theia parses the text using a tokenizer and creates bytecode which can be executed by the TheiaVM.

This project was developed using Netbeans IDE 8.1 and can be opened directly in Netbeans by checking out this repo and going to `File > Open Project ...`.

Beware of the fact that Theia might contain errors. You are using Theia on your own risk. You can take a look at the bugs [here](https://github.com/maxstrauch/theia/issues).

# Features

 - Theia supports LOOP, WHILE and GOTO programs with "extensions for convenience" (the `if` statement and expression evaluation, e.g. `x1 + 3` or `2 + 5`)
 - Simple grammar (see bellow)
 - Nice GUI using the cute Nimbus LaF
 - Preview compiled bytecode (see screenshot)
 - Syntax highlighted editor
 - All numbers are natural numbers ranging from `0` to `2^31 = 2,147,483,648`
 - Detailed syntax error messages
 - Uses only 13 instructions for the TheiaVM

# Grammar

The grammar for the languages LOOP, WHILE and GOTO is given hereinafter. The "entry point" for LOOP is `loop_prog`, for WHILE `while_prog` and for GOTO programs `goto_prog`.

	loop_prog   ::= lstmt

	while_prog  ::= wstmt

	goto_prog   ::= (NUM ':' gstmt ';')* NUM ':' gstmt

	lstmt       ::= lstmt ';' lstmt 
	              | var ':=' expr 
	              | 'loop' var 'do' lstmt 'end' 
	              | 'if' cond 'then' lstmt 'else' lstmt 'end'

	wstmt       ::= wstmt ';' wstmt 
	              | var ':=' expr 
	              | 'while' var '!=' NUM 'do' wstmt 'end' 
	              | 'if' cond 'then' wstmt 'else' wstmt 'end'

	gstmt       ::= var ':=' expr 
	              | 'if' var '=' NUM 'goto' NUM
	              | 'if' cond 'then' gstmt 'else' gstmt 'end'

	expr        ::= VAR | NUM | expr ('+'|'-'|'*') expr

	cond        ::= VAR | NUM | cond ('='|'<=') cond

	VAR         ::= 'x' NUM
	NUM         ::= [0-9]+

# Bytecode instructions

The source file `de.theia.vm.PrettyPrint` contains all necessary information to infer the virtual machine instructions and arguments. 

# Download

A downloadable version of Theia can be found [here (in `dist/theia.jar`)](/dist/theia.jar?raw=true). The SHA1 sum of Theia version 1.0 is:

	19d72034c74b731205999ec042c82c89117a81dc  theia.jar

# License

License: creative commons 4.0, by-sa

`This program is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.`
