To ensure that mixing in VFS does not increase the bug-count for typical non-VFS usage, 
we've wired up RubySpec to run.

Make sure VFS maven project has been built

	mvn install

Now, the RubySpec tests can be run using the command

	jruby -S rake rubyspec

The report will be placed in 

	target/rubyspec.html

If you need to compare with-VFS to without-VFS, simply add the rake target 'novfs' before 'rubyspec'

	jruby -S rake novfs rubyspec

A report will be placed in

	target/rubyspec-novfs.html

To run the specs in 1.9 compatibility mode, add the '19' task:

	jruby -S rake 19 rubyspec

The report will be placed in 

	target/rubyspec-19.html

1.9 can also be used with novfs.

To run just a single test using VFS:

     jruby -J-classpath target/dependencies/jboss-vfs.jar -Ilib -rvfs -S spec target/jruby/spec/ruby/core/file/chmod_spec.rb

or, for 1.9:
     jruby --1.9 -J-classpath target/dependencies/jboss-vfs.jar -Ilib -rvfs -S spec target/jruby/spec/ruby/core/file/chmod_spec.rb
