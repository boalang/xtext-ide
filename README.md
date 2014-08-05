# Boa's XText-based IDE #

This is Boa's XText-based IDE, which is a plugin for Eclipse.


## Pre-requisites ##

**INSTALL XTEXT FIRST!** XText is required to compile the IDE.  You can
download it here:

[http://www.eclipse.org/Xtext/download.html](http://www.eclipse.org/Xtext/download.html)

After checking out this project, make sure you run setup.sh to create some
necessary (empty) directories!  Otherwise Eclipse will refuse to build the
projects.


## Project Organization ##

This repository contains 4 Eclipse projects:

* edu.iastate.cs.boa edu.iastate.cs.boa.sdk edu.iastate.cs.boa.tests
* edu.iastate.cs.boa.ui

Each project contains a 'src' sub-dir, which contains files you *can* modify
and src-gen, xtend-gen sub-dirs that you should *not* modify (they are
generated)!

Important files:

* edu.iastate.cs.boa - /src/edu/iastate/cs/boa/Boa.xtext (the grammar file)
* edu.iastate.cs.boa - /src/edu/iastate/cs/boa/GenerateBoa.mwe2 (the modeling engine workflow file)

If you modify the input grammar (Boa.xtext) or modeling engine workflow file
(GenerateBoa.mwe2) you must rebuild!


## Re-generating Files ##

Open the GenerateBoa.mwe2 file and Run As -> MWE2 Workflow.  This can require a
lot of memory/time!


## Running the IDE ##

Once you have all files built, you can run the custom IDE:

* Right-click on the 'edu.iastate.cs.boa' or 'edu.iastate.cs.boa.ui' project
	* Run-As
		* Eclipse Application

Note that anytime you make changes to source, you have to restart the custom
IDE.


## Debugging the IDE ##

Since the IDE is just Java code, you can also choose to debug it.

* Right-click on the 'edu.iastate.cs.boa' or 'edu.iastate.cs.boa.ui' project
	* Debug-As
		* Eclipse Application

This will launch the IDE in a separate Eclipse instance.  You can use the
original instance to debug it, by setting breakpoints, pausing it, etc just as
you would any other Java application.
