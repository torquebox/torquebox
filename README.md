Requirements
------------
* Maven 3
* Configuration of the JBoss Maven repository in settings.xml

Dependencies
------------

TorqueBox depends on polyglot: https://github.com/projectodd/jboss-polyglot

Polyglot is published as a snapshot, so you may need to run mvn with the
-U option to check for updates if you run into build issues.

Building
--------

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

Running
-------

When built, set the follow environment variables, but replace ~/src/torquebox with the path to your local repo.

    export TORQUEBOX_HOME=~/src/torquebox/build/assembly/target/stage/torquebox
    export PATH=$TORQUEBOX_HOME/jruby/bin:$PATH

Then start as usual with ```torquebox run```.

