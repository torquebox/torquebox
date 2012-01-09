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

After successfully building it, you'll probably want to run it.  To do
so, set `TORQUEBOX_HOME`, and add `$TORQUEBOX_HOME/jruby/bin` to your
`$PATH`, like so:

    export TORQUEBOX_HOME=$PWD/build/assembly/target/stage/torquebox
    export PATH=$TORQUEBOX_HOME/jruby/bin:$PATH
    
You can then use the `torquebox` command to control your creation. Run
it without parameters to see a list of its supported subcommands.

