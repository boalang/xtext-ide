# Boa IDE TODO list #

This list is roughly organized by priority.  Where possible, notes are included
(such as links to examples/howtos) and an estimate of effort is given.


## Support Scoping / Type Linking ##

Effort: High

There is no scope provider for Boa.  This is needed in order to properly
support many other features, including the type linking.

See: /edu.iastate.cs.boa/src/edu/iastate/cs/boa/scoping/BoaScopeProvider.xtend


## More Validators ##

Effort: Medium

Right now there are only a couple example validators.  Any type check or
semantic error needs validated.

See: /edu.iastate.cs.boa/src/edu/iastate/cs/boa/validation/BoaFunctionValidator.xtend


## More Quick Fixes ##

Effort: Low

We have a few example quick fixes.  More are needed.  This depends on the
validator code being implemented, as the errors generated there are what we
provide quick fixes for.

See: /edu.iastate.cs.boa.ui/src/edu/iastate/cs/boa/ui/quickfix/BoaQuickfixProvider.xtend


## Better Content Assist ##

Effort: High

Support better auto-completion suggestions.  The defaults are basically just
templates (which we need more of) and the next available parser token.

See: /edu.iastate.cs.boa.ui/src/edu/iastate/cs/boa/ui/contentassist/BoaProposalProvider.xtend


## More Templates ##

Effort: Low

Support more/better templates.  Also the template editor has way too many
categories right now.

See: /edu.iastate.cs.boa.ui/templates/templates.xml


## Better Outlining/Labeling ##

Effort: Medium

The default outline/labels for the AST are pretty useless.

See: /edu.iastate.cs.boa.ui/src/edu/iastate/cs/boa/ui/outline/BoaOutlineTreeProvider.xtend
     /edu.iastate.cs.boa.ui/src/edu/iastate/cs/boa/ui/labeling/BoaLabelProvider.xtend
     /edu.iastate.cs.boa.ui/src/edu/iastate/cs/boa/ui/labeling/BoaDescriptionLabelProvider.xtend


## Boa Launch Configuration ##

Effort: Medium

Right now we have a custom Boa menu with a 'run' under it (and a button linked
to this action).  Instead, we should create an Eclipse launcher for Boa.  This
would allow doing things like 'run-as -> Boa Application' inside Boa source.
Or setting up a launch configuration for one input, and another for a
different.  Launch configurations would allow avoiding the input dialog popping
up each time, as the configuration would hard code an input.

[https://www.eclipse.org/articles/Article-Launch-Framework/launch.html](Launch Framework How-To)
[https://searchcode.com/codesearch/view/4351921/](Example Launch Delegate)


## Use XText as a Generator ##

Effort: Very High

Right now we have a custom compiler.  Eventually we want to be able to compile
locally (not on the server) so we will want a generator that produces the
Hadoop code.

See: /edu.iastate.cs.boa/src/edu/iastate/cs/boa/generator/BoaGenerator.xtend


## Package Hadoop ##

Effort: Medium

We want to include a pre-configured (as pseudo-distributed) Hadoop install so
that the locally generated Boa programs can run on a small example dataset.
This will help ease debugging/testing.


## Include a Small/Example Dataset ##

Effort: Low

We want a small dataset included, so that Boa programs can run locally on it
and ease debugging and testing.
