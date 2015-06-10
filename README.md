# TorqueBox

This is TorqueBox 4 and represents a substantial change in direction
from previous TorqueBox releases. We're moving to a lightweight,
embedded model that runs without any Java application server, at the
expense of a few of the more enterprisy features. For users that want
to run in a Java application server or need those enterprisy features,
we'll provide a way to take your TorqueBox application and run it
unmodified on a stock [WildFly][wildfly] installation.

## Documentation

We have various guides available to help with installation, migration,
usage, and deployment of TorqueBox 4.

- [Installation Guide](docs/installation.md)
- [Migration Guide](docs/migration.md)
- [Web Guide](docs/web.md)
- [Messaging Guide](docs/messaging.md)
- [Scheduling Guide](docs/scheduling.md)
- [Caching Guide](docs/caching.md)
- [Executable Jar Guide](docs/jar.md)
- [Wildfly Guide](docs/wildfly.md)


## Quickstart for running TorqueBox

### Requirements

TorqueBox requires JRuby 1.7.x (in 1.9 or 2.0 mode) or JRuby 9.x.x
running on Java 7+. The code has only been tested on JRuby 1.7.6 and
higher but should work on earlier versions.

### Installation

Read the [Installation Guide](docs/installation.md) for installation
details.

### Running directly

From inside your Rack application's root directory:
    
    torquebox run

### Rails

Ensure `torquebox` is in your `Gemfile`, then:

    rails s torquebox

### Rack

    rackup -s torquebox


## Motivation

We want a smaller, more modular TorqueBox that is easier to get
started with, embeddable, and lets users bring in additional
functionality as-needed. More details of our motivation and community
feedback are expressed in [an email thread][tb_future_thread] from the
torquebox-user mailing list.

## Technology

TorqueBox runs on JRuby and sits on top of a new lightweight, pluggable,
polyglot server codenamed [WunderBoss][wunderboss] (at least for
now). All the new features of TorqueBox will be implemented in
WunderBoss then exposed via a Ruby API in the TorqueBox project. This
lets other projects, in other languages, reuse the same functionality
by creating small language-specific API wrappers.

The web portion of WunderBoss uses [JBoss Undertow][undertow], which
is also the same web server used in [WildFly][wildfly] (the successor
to JBoss Application Server).

We aim to reuse the same underlying components as WildFly so that
TorqueBox applications can run on top of WildFly in addition to
running without it, to give users an option between a full-blown Java
application server and a very lightweight, minimal server.


## Current status

Right now TorqueBox 4 provides a high-performance Rack implementation
for web applications and basic APIs for messaging, caching, and
scheduled jobs. The Rack support is considered production-ready, but
the messaging, caching, and scheduling implementations are still in a
bit more flux.

## Roadmap

We're developing TorqueBox 4 while also maintaining TorqueBox 3, and
we expect it to take some time before TorqueBox 4 comes out of alpha
and betas and into a final release.

## Building TorqueBox

    bundle install
    rake build

## Running specs

    rake spec

## Running integration tests

Make sure `phantomjs` is available on your $PATH -
http://phantomjs.org/download.html.

The first time you run the integration tests may take longer as
bundler gets invoked for each test application to install its
dependencies. Subsequent runs with the same JRuby installation should
be faster.

    cd integration-tests
    rake spec

## Running a single integration test

    cd integration-tests
    SPEC=spec/basic_sinatra_spec.rb rake spec

## Running specs with more verbose output

    DEBUG=true rake spec

## Running all integration test variants (disk, jar, wildfly)

There are several variants of integration tests. Each runs the same
applications and same specs, but packaged in different ways. This
takes quite a bit longer, but is what CI runs.

    rake spec:all

## Installing from source

    rake install

## Releasing

### Preparation

TorqueBox 4 is released from the `master` branch of
[torquebox/torquebox-release][release_repo].

Set up this repository as an additional remote for your workspace:

    git remote add release git@github.com:torquebox/torquebox-release.git

Ensure that the `master` branch has the contents you wish to release.  Using the `-f`
flag to force is allowed in this case, since the **torquebox-release** repository is not
a public-facing human-cloneable repository.

    git push release master:master -f


### Perform the build

Using the [build system](http://projectodd.ci.cloudbees.com/), select
the **torquebox4-release** job, entering in the branch to release from
(usually 'master'), the version to release, and the next version
after release.

If something goes wrong in the release job and it needs to run again,
be sure to reset the torquebox-release repository with the correct code first:

    git push release master:master -f

### Deploy RubyGems

After the job complete successfully, the generated RubyGems will need
to be manually deployed. Download them from the job and push each
using:

    gem push <gem_name>.gem

You'll have to be an owner of the gems to do this. Bug bbrowning,
bobmcw, or tcrawley if you are not.

### Publish the release API documentation

The release API docs are built by the release job on CI. Download
those and put them a _4x_docs/<version>/yardoc folder in the
torquebox.org git repo.

### Push changes from the release repository to the official repository

    git fetch release --tags
    git merge release/master
    git push origin master
    git push origin <release_tag>

### Release the project in JIRA

### Announce it

#### Post it on `torquebox.org`

#### Notify the `torquebox-users@` list

#### Tweet it.

#### Set the /topic in #torquebox IRC channel using ChanServ (if you can).


[tb_future_thread]: http://markmail.org/thread/4ffelg3qklycwhfo
[community]: http://torquebox.org/community/
[wunderboss]: https://github.com/projectodd/wunderboss
[undertow]: http://undertow.io/
[wildfly]: http://wildfly.org/
[release_repo]: http://github.com/torquebox/torquebox-release
