Requirements
------------
* Maven 3
* Configuration of the JBoss Maven repository in settings.xml

Building
--------

Install the project using the provided settings.xml:

    mvn -s build-support/settings.xml install


If you will be building the project often, you'll want to
create/modify your own ~/.m2/settings.xml file.

If you're a regular JBoss developer, see:

* http://community.jboss.org/wiki/MavenGettingStarted-Developers

Otherwise, see: 

* http://community.jboss.org/wiki/MavenGettingStarted-Users

Once your repositories are configured, simply type:

    mvn install


Layout
------

* `build-support/` Contains code related to building TorqueBox, but not 
  involved at runtime.

* `components/` Includes all code required at runtime within the AS (Java-centric).

* `containers/` Includes outside-of-the-AS container code (Ruby-centric).

* `clients/` Includes client API gems (Ruby-centric).

* `assemblage/` Contains instructions for building the assembled final
  deliverables and distributions.

* `integration-tests/` Contains a series of Arquillian-based tests
  against the packaged distribution.

* `dist/` Contains the instructions for creating the final binary
 distributable in .zip format.

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
