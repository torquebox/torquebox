# TorqueBox

This is TorqueBox 4.0 and represents a substantial change in direction
from previous TorqueBox releases. We're moving to a lightweight,
embedded model that runs without any Java application server, at the
expense of a few of the more enterprisy features. For users that want
to run in a Java application server or need those enterprisy features,
we'll provide a way to take your TorqueBox application and run it
unmodified on a stock [WildFly][wildfly] installation.


## Running TorqueBox

### Requirements

TorqueBox requires JRuby 1.7.x running on Java 7+ in Ruby 1.9 or 2.0
mode. The code has only been tested on JRuby 1.7.6 and higher but
should work on earlier versions.

### Running directly

From inside your Rack application's root directory:

    gem install torquebox
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

We aim to reuse the same underlying components as WildFly where it
makes sense, and bring our own where it doesn't. Eventually, we hope
that TorqueBox can run on top of WildFly in addition to running
without it, to give users an option between a full-blown Java
application server and a very lightweight, minimal server. The
lightweight, minimal server is what we'll focus on at first, since
TorqueBox 3 already provides the full application server experience.


## Current Status

Right now TorqueBox 4 just provides a basic, high-performance Rack
implementation. It should outperform anything else out there, but if
you find any cases where this is not true please [let us
know][community]. It is substantially faster than TorqueBox 3, which
is already one of the fastest servers. We'll work on publishing
benchmarks as time permits, and we encourage community users to do
their own performance tests.

## Roadmap

We're developing TorqueBox 4 while also maintaining TorqueBox 3, and
we expect it to take some time before TorqueBox 4 comes out of alpha
and betas and into a final release.

Our first goal is for TorqueBox to become the best JRuby web server
option. To us this means high performance, support for newer web
technologies (WebSockets, Server Sent Events, SPDY, etc), lightweight,
and simple to use. It may mean something else to you, and if it does
please [let us know][community]. The basic Rack spec is implemented
right now, with the rack.hijack API coming soon.

What features from TorqueBox (or elsewhere) we tackle after web is up
to you. It could be messaging, caching, scheduled jobs, daemons, or
something completely different. Please [let us know][community] what
you'd like to see.

Long-term we do expect TorqueBox to run on top of WildFly for users
that still want the full Java application server (or are trying to
sneak Ruby into a Java shop). But this will just be an option, not the
default.

## Building TorqueBox

    bundle install
    rake build

## Running specs

    rake spec

## Running integration tests

Make sure `phantomjs` is available on your $PATH -
http://phantomjs.org/download.html

    cd integration-tests
    rake spec

## Running a single integration test

    cd integration-tests
    SPEC=spec/basic_sinatra_spec.rb rake spec

## Running specs with more verbose output

    DEBUG=true rake spec

## Installing from source

    rake install

## Releasing

    rake clean
    rake release


[tb_future_thread]: http://markmail.org/thread/4ffelg3qklycwhfo
[community]: http://torquebox.org/community/
[wunderboss]: https://github.com/projectodd/wunderboss
[undertow]: http://undertow.io/
[wildfly]: http://wildfly.org/
