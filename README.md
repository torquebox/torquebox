# TorqBox

This is a prototype for the next-generation TorqueBox. We're assigning
the codename 'TorqBox' to this effort because we need some way to
differentiate it from TorqueBox itself until this prototype matures
enough to replace TorqueBox.

Why 'TorqBox'? Mainly because we wanted a new Ruby gem name to release
under that's not 'torquebox' and it's close enough to TorqueBox that
the connection is obvious. Plus, search engines should still redirect
users to the right place for help. Once TorqBox graduates out of a
prototype, we'll remove the codename and release it under the regular
'torquebox' gem.


## Running TorqBox

### Requirements

TorqBox requires JRuby 1.7.x. The code has only been tested on JRuby
1.7.6 and higher but should work on earlier versions.

### Running directly

From inside your Rack application's root directory:

    gem install torqbox
    torqbox

### Rails

Ensure `torqbox` is in your `Gemfile`, then:

    rails s torqbox

### Rack

    rackup -s torqbox


## Motivation

We want a smaller, more modular TorqueBox that is easier to get
started with, embeddable, and lets users bring in additional
functionality as-needed. More details of our motivation and community
feedback are expressed in [an email thread][tb_future_thread] from the
torquebox-user mailing list.

## Technology

TorqBox runs on JRuby and sits on top of a new lightweight, pluggable,
polyglot server codenamed [WunderBoss][wunderboss] (at least for
now). All the new features of TorqBox will be implemented in
WunderBoss then exposed via a Ruby API in the TorqBox project. This
lets other projects, in other languages, reuse the same functionality
by creating small language-specific API wrappers.

The web portion of WunderBoss uses [JBoss Undertow][undertow], which
is also the same web server used in [WildFly][wildfly] (the successor
to JBoss Application Server).

We aim to reuse the same underlying components as WildFly where it
makes sense, and bring our own where it doesn't. Eventually, we hope
that TorqBox can run on top of WildFly in addition to running without
it, to give users an option between a full-blown Java application
server and a very lightweight, minimal server. The lightweight,
minimal server is what we'll focus on at first, since TorqueBox
already provides the full application server experience.


## Current Status

Right now `torqbox` just provides a basic, high-performance Rack
implementation. It should outperform anything else out there, but if
you find any cases where this is not true please [let us
know][community]. It is substantially faster than TorqueBox 3, which
is already one of the fastest servers. We'll work on publishing
benchmarks as time permits, and we encourage community users to do
their own performance tests.

## Roadmap

We're developing TorqBox while also maintaining TorqueBox, and we
expect it to take some time before the TorqBox prototype becomes
mature enough to be called a new major version of TorqueBox.

Our first goal is for TorqBox to become the best JRuby web server
option. To us this means high performance, support for newer web
technologies (WebSockets, Server Sent Events, SPDY, etc), lightweight,
and simple to use. It may mean something else to you, and if it does
please [let us know][community]. The basic Rack spec is implemented
right now, with the rack.hijack API coming soon.

What features from TorqueBox (or elsewhere) we tackle after web is up
to you. It could be messaging, caching, scheduled jobs, daemons, or
something completely different. Please [let us know][community] what
you'd like to see.

Long-term we do expect TorqBox to run on top of [WildFly][wildfly] for
users that still want the full Java application server (or are trying
to sneak Ruby into a Java shop). But this will just be an option, not
the default.


[tb_future_thread]: http://markmail.org/thread/4ffelg3qklycwhfo
[community]: http://torquebox.org/community/
[wunderboss]: https://github.com/projectodd/wunderboss
[undertow]: http://undertow.io/
[wildfly]: http://wildfly.org/
