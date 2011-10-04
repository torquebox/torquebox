Requirements
------------
* Maven 3
* Configuration of the JBoss Maven repository in settings.xml

Building
--------

TorqueBox depends on polyglot: https://github.com/projectodd/jboss-polyglot

You'll need to pull and build it (following its README) before building 
TorqueBox, and it will stuff its artifacts into ~/.m2/

Install the project using the provided settings.xml:

    mvn -s support/settings.xml install

If you will be building the project often, you'll want to
create/modify your own ~/.m2/settings.xml file.

If you're a regular JBoss developer, see:

* http://community.jboss.org/wiki/MavenGettingStarted-Developers

Otherwise, see: 

* http://community.jboss.org/wiki/MavenGettingStarted-Users

Once your repositories are configured, simply type:

    mvn install


