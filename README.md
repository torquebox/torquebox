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
