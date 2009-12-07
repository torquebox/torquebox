To ensure that mixing in VFS does not increase the bug-count for typical non-VFS usage, 
we've wired up RubySpec to run.

First, ensure you have the 'mspec' gem installed.

	jruby -S gem instsall mspec

Secondly, make sure VFS maven project has been built

	mvn package

Now, the RubySpec tests can be run using the command

	jruby -S rake rubyspec

The report will be placed in 

	target/rubyspec.html

If you need to compare with-VFS to without-VFS, simply add the rake target 'novfs' before 'rubyspec'

	jruby -S rake novfs rubyspec

A report will be placed in

	target/rubyspec-novfs.html


