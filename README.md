Requirements
------------
* Maven 3
* Bob's jruby-maven-plugins fork (github.com/bobmcwhirter/jruby-maven-plugins), built and installed
* Configuration of the JBoss Maven repository in your settings.xml
  * http://community.jboss.org/wiki/MavenGettingStarted-Users
  * http://community.jboss.org/wiki/MavenGettingStarted-Developers

Building
--------
Simply type

    mvn install

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

    ./assemblage/assembly/stage/torquebox-${VERSION}

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
