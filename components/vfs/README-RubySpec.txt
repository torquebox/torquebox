To ensure that mixing in VFS does not increase the bug-count for typical non-VFS usage, 
we've wired up RubySpec to run.

First, ensure you have the 'mspec' and 'rspec' (1.3.x) gems installed.

	jruby -S gem install mspec
  jruby -S gem install rspec -v=1.3.1

Secondly, make sure VFS maven project has been built

	mvn install

Now, the RubySpec tests can be run using the command

	jruby -S rake rubyspec

The report will be placed in 

	target/rubyspec.html

If you need to compare with-VFS to without-VFS, simply add the rake target 'novfs' before 'rubyspec'

	jruby -S rake novfs rubyspec

A report will be placed in

	target/rubyspec-novfs.html


To run just a single test using VFS:

     jruby -J-classpath target/dependencies/jboss-vfs.jar -Ilib -rvfs -S spec target/jruby/spec/ruby/core/file/chmod_spec.rb
