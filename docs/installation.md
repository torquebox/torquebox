# @title Installation Guide

# Installation

## Official releases

Every official release of TorqueBox is published to
[rubygems.org][tb_gems]. Until TorqueBox 4 comes out of alpha or beta,
you'll need to make sure to include a version identifier in your
Gemfile to pick up the prerelease gems.

For example:

    gem "torquebox", "4.0.0.alpha1"

You can also pick and choose various TorqueBox components if you don't
want the entire suite of services. To do that, remove the `torquebox`
gem from your Gemfile and add the appropriate individual gems.

An example of using only the web and scheduling services:

    gem "torquebox-web", "4.0.0.alpha1"
    gem "torquebox-scheduling", "4.0.0.alpha1"

The list of official TorqueBox gems are:

- torquebox (aggregate of all the other gems if you want the entire stack)
- torquebox-core (dependency of others - no need to specify in your Gemfile)
- torquebox-caching
- torquebox-messaging
- torquebox-scheduling
- torquebox-web

## Incremental builds from our CI server

Every successful build from our continuous integration server
publishes updated gems to our incremental gem repository. To see the
latest incremental builds and for instructions on how to consume the
incremental gems head over to our [TorqueBox 4 incremental builds
page][4x_builds].

[tb_gems]: http://rubygems.org/gems/torquebox
[4x_builds]: http://torquebox.org/4x/builds/
