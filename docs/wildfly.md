# @title Deploying an Application to WildFly

# WildFly Deployment

One of the primary goals for TorqueBox 4 is the removal of the ancient
AS7 fork we lugged around in TorqueBox 3.x. This eliminates the need to
install and deploy your apps into a "container" to use the TorqueBox
gems.

But here's the trade-off: app server containers can simplify the
configuration of features -- e.g. security, monitoring, clustering --
for all the applications deployed to it.

And since each TorqueBox gem automatically benefits in some way
from being clustered, we wanted to *facilitate* app server deployment
but not actually *require* it. Further, we didn't want to require any
tweaking of the stock "vanilla" configuration provided by the app
server. This meant using the standard deployment protocol for all Java
app servers: war files.

TorqueBox intentionally uses the same services as [WildFly], the
community-supported upstream project for the commercially-supported
[JBoss EAP] product. And these are the containers we'll initially
support.

## WildFly

There are lots of resources for installing and administering WildFly,
and frankly, we love being able to refer you to those rather than
write them ourselves. :)

Thankfully, installing WildFly is trivial:

    $ wget http://download.jboss.org/wildfly/8.2.0.Final/wildfly-8.2.0.Final.zip
    $ unzip wildfly-8.2.0.Final.zip

Downloading and unpacking it somewhere are all there is to it. Running
it is easy, too:

    $ wildfly-8.2.0.Final/bin/standalone.sh

Pass it `-h` to see what options it supports. The main one you'll use
is `-c` which refers to one of its config files beneath
`standalone/configuration`. The default config doesn't include
HornetQ, for example, so to use TorqueBox messaging, you'll need to
start WildFly as follows:

    $ wildfly-8.2.0.Final/bin/standalone.sh -c standalone-full.xml

And if you want clustering...

    $ wildfly-8.2.0.Final/bin/standalone.sh -c standalone-full-ha.xml

You can create your own, of course, too.

### Creating a war file

TorqueBox war files require a bit of special config: a couple of jars
of "glue code", a properties file to trigger that code, a couple of
tags in `web.xml`, and a `jboss-deployment-structure.xml` to link the
deployment to the necessary WildFly modules. Luckily, the `torquebox
war` command handles all of that for you - it generates a [jar] of
your application, then places it (along with the aforementioned
configuration) in to a war file:

    $ bundle exec torquebox war

The `torquebox war` command provides a number of configuration options,
all of which can be specified as command line arguments, many of which
are the same as the ones available for the [jar] task.

For a full list of options, try:

    $ torquebox war -h

## Deploying to WildFly

Once you have a war file, it's a simple matter of making it known to
your WildFly server. The easiest way to do that is to copy it to a
directory that is monitored by WildFly for artifacts to deploy.
Assuming you installed WildFly in `/srv/wildfly`, that path is
`/srv/wildfly/standalone/deployments`. For example:

    $ bundle exec torquebox war
    $ cp myapp.war /srv/wildfly/standalone/deployments

Alternatively,

    $ bundle exec torquebox war --destination /srv/wildfly/standalone/deployments

If not already running, fire up WildFly to see your deployed app:

    $ /srv/wildfly/bin/standalone.sh -c standalone-full.xml

## Context paths in WildFly

The URL for your handler will include a context path corresponding to
the base name of your deployed war file. To override this and set your
context path to "/" instead, provide a context-path when building the war:

    $ bundle exec torquebox war --destination /srv/wildfly/standalone/deployments --context-path /

or name your war file `ROOT.war`:

    $ bundle exec torquebox war --destination /srv/wildfly/standalone/deployments --name ROOT.war


[WildFly]: http://wildfly.org
[JBoss EAP]: http://www.jboss.org/products/eap/overview/
[jar]: ./file.jar.html
