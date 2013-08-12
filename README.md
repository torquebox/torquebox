# What is TorqueBox?

TorqueBox is a Ruby application server built on JBoss AS7 and
JRuby. It supports Rack-based web frameworks and provides simple Ruby
interfaces to standard enterprisey services, including scheduled jobs,
caching, messaging, and services.

# Useful Resources

* Issues - https://issues.jboss.org/browse/TORQUE

* CI - https://projectodd.ci.cloudbees.com/job/torquebox-incremental/

# Development

## Requirements

* Maven 3
* Configuration of the JBoss Maven repository in settings.xml

## Dependencies

TorqueBox depends on polyglot: https://github.com/projectodd/jboss-polyglot

If you chase a TorqueBox issue down and run into
org.projectodd.polyglot classes, this is where their source is.

## Building

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

## Running

After successfully building it, you'll probably want to run it.  To do
so, set `TORQUEBOX_HOME`, and add `$TORQUEBOX_HOME/jruby/bin` to your
`$PATH`, like so:

    export TORQUEBOX_HOME=$PWD/build/assembly/target/stage/torquebox
    export PATH=$TORQUEBOX_HOME/jruby/bin:$PATH
    
You can then use the `torquebox` command to control your creation. Run
it without parameters to see a list of its supported subcommands.

## Testing

All unit tests will be run during the build process, but tests can be run independently with the following command:

    mvn -s support/settings.xml test

The integration tests (a.k.a. integs), which are not run as part of the main build, can be run like this:

    cd integration-tests
    mvn test -s ../support/settings.xml

Or a single integration test can be run like this:

    mvn test -s ../support/settings.xml -Dspec=spec/session_handling_spec.rb

If you wish to skip the unit tests during the build process (to speed things up) you can add the `-Dmaven.test.skip=true` option when running the `mvn install` command.

