Requirements
------------
* Maven 3
* A special jruby-maven-plugins fork (http://github.com/torquebox/jruby-maven-plugins/tree/next <-- note the 'next' branch!), built and installed
* Configuration of the JBoss Maven repository in your settings.xml

Building
--------

If you're a JBoss developer, you may setup your maven `settings.xml` to include
access to the JBoss developer repository.

* http://community.jboss.org/wiki/MavenGettingStarted-Developers

Then, simply type

    mvn install

If you're not a JBoss developer, you may setup your maven `settings.xml` to
include the public JBoss repository

* http://community.jboss.org/wiki/MavenGettingStarted-Users

And then simply type

    mvn install

If you don't wish to modify your user `settings.xml`, you may use the `settings.xml`
that is included:

    mvn -s settings.xml install

Layout
------

* `build-support/` Contains code related to building TorqueBox, but not 
  involved at runtime.

* `components/` Includes all code required at runtime.

* `assemblage/` Contains instructions for building the assembled final
  deliverables and distributions.

Development Tips
----------------

If you're actively developing portions of the TorqueBox codebase,
it's useful to define $TORQUEBOX_HOME around the directory

    ./assemblage/assembly/target/stage/torquebox-${VERSION}

And then define JBOSS_HOME and JRUBY_HOME per usual

    JBOSS_HOME=$TORQUEBOX_HOME/jboss
    JRUBY_HOME=$TORQUEBOX_HOME/jruby

This assembly is created when you execute `mvn install` from the top-level
project.  

As you built integration projects using `mvn install`, they will
install themselves into the assembly, if present.  Likewise, dependency
gems will be installed into the JRuby RubyGems path.  Plus, changes
to the VFS system will install itself into the JRuby lib directory and gem
repository as-needed.  

It's like magic.
