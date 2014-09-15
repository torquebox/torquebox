# @title TorqueBox 3 -> 4 Migration Guide

# Migrating from TorqueBox 3 to TorqueBox 4

TorqueBox 4 is quite different from TorqueBox 3 in several ways, the
largest being:

* **An Application Server is no longer required.** TorqueBox 4 ships
  as a collection of gems, and can be used in a standalone JRuby
  application - you no longer are required to download and manage a
  monolithic container. Even though the container isn't required, you
  can still deploy TorqueBox 4 to a [WildFly](http://wildfly.org/)
  server to take advantage of clustering, load-balancing, and failover
  features. For more details on using WildFly, see our
  [WildFly Guide](wildfly.md). For details on gem usage, see our
  [Installation Guilde](installation.md).
* **Applications can now be packaged as executable jar files.**
  TorqueBox 4 supports packaging your entire application (including
  gems and JRuby itself) as a executable jar file that can run
  anywhere Java is available without any other dependencies. For more
  details, see our [Jar Guide](jar.md).
* **Everything is started/configured dynamically.** In TorqueBox 3,
  the majority of your application's resource setup was static, and
  had to be in `torquebox.yml` or `torquebox.rb`. In TorqueBox 4, all
  resources can be set up dynamically, at runtime (more on this
  below).

## Application Initialization

In TorqueBox 3, most of your resources had to be set up statically at
deploy time, either through `torquebox.yml` or `torquebox.rb`. In
TorqueBox 4, neither of those files are supported, and you instead set
up resources at runtime from within your application code.

## Web

### SockJS

## Messaging

Message Processors -> [listen](TorqueBox/Messaging/Destination.html#listen-instance_method)

Synchronous Message Processors -> [response](TorqueBox/Messaging/Queue.html#respond-instance_method)

not supported:

- queue enumeration
- xa

No Stomp.
=======

## Scheduling

Changes:

- no job classes, just blocks
- no job timeouts (yet)
- no passing configuration to jobs

## Caching

## XA?
