# @title Deploying an Application as a Jar

# Jar Deployment

Using the `torquebox jar` command, you can generate an jar file that
(optionally) includes JRuby, your application, and all of its
dependencies (if you are using bundler). This gives you an artifact
that can be launched anywhere java (version 7 or higher) is
available - no JRuby install is required. You run the jar with:

    $ java -jar myapp.jar

## Building the jar

The `torquebox-core` gem provides a `torquebox` binary that provides
command-line utilities for managing TorqueBox applications. One of
those utilities is `torquebox jar`. We'll walk through a simple web
application to demonstrate its usage (for general information on
web-based applications with TorqueBox, see the [Web Guide]).

First, a `Gemfile`. We'll use [Sinatra], so we need to bring its gem
in, along with `torquebox-web`:

    source "https://rubygems.org"

    gem "sinatra", "1.4.5"
    gem "torquebox-web", "4.0.0.alpha1"

Then, the application itself. Let's put it at `app.rb` at the root of
our project:

    require 'sinatra'

    get '/' do
      "ahoyhoy!<br>
       FOO=#{ENV['FOO']}<br>
       BAR=#{ENV['BAR']}"
    end

Lastly, we'll need a standard `config.ru` to start the application:

    require './app'

    run Sinatra::Application

Now we can generate the jar. This will vendor our gems inside the jar,
along with JRuby itself:

    $ bundle exec torquebox jar

You should see output like:

    15:53:24.604 INFO  Bundling gem dependencies
    15:53:44.530 INFO  Writing ./jar-example.jar

By default, the jar command uses the name of the current directory as
the jar name - you can override that with the `--name` option. In my
case, the dir was `jar-example`. It also creates the jar in the
current directory - that can be overridden with `--destination`.

Since we're bundling JRuby, we end up with a rather large file (33Mb
for this example), but that's the price we pay for a portable
artifact. If you will be running the jar on a machine that has a JRuby
install, you can disable the inclusion of JRuby, resulting in a much
smaller artifact (we cover that in more detail below).

## Running the jar

Run the jar with (remember, your jar name may vary):

    $ java -jar jar-example.jar

You should see output like:

    16:02:29.061 INFO  [org.projectodd.wunderboss] (main) Initializing application as ruby
    16:02:32.940 INFO  [org.projectodd.wunderboss.web.Web] (main) Registered web context /
    16:02:32.942 INFO  [TorqueBox::Web::Server] (main) Starting TorqueBox::Web::Server 'default'
    16:02:33.258 INFO  [TorqueBox::Web::Server] (main) Listening for HTTP requests on localhost:8080

If you then visit <http://localhost:8080> in a browser, you should get
a response. But notice that the response doesn't include our
environment variables.

## Environment variables

When running the jar file, you can provide environment variables as
you normally would, either exported or on the command-line:

    $ FOO=foo BAR=bar java -jar jar-example.jar

Or, you can include them in the jar itself. Try:

    $ bundle exec torquebox jar --envvar FOO=foo --envvar BAR=bar

and run it again with:

    $ java -jar jar-example.jar

Then visit <http://localhost:8080> to see the request complete with
the environment variable values. Note that the two options above
aren't mutually exclusive - you can set some variables as part of the
jar build process, and provide others (or override ones set in the
jar) at the command-line.

## Using an existing JRuby install

If you have an existing JRuby install, and don't want to include one
in the jar (or override the one that is included), you can do so by
specifying a JRuby home dir. But first, let's build a jar that doesn't
include JRuby:

    $ bundle exec torquebox jar --no-include-jruby

This generates *much* smaller jar file - 13Mb for this example. Now,
we can run it, either with:

    $ JRUBY_HOME=/path/to/jruby java -jar jar-example.jar

or:

    $ java -Djruby.home=/path/to/jruby -jar jar-example.jar

You can also create a jar without bundled gems, if they are available
in the local JRuby install:

    $ bundle exec torquebox jar --no-include-jruby --no-bundle-gems

This results in an even smaller jar (3.7Mb).

## Initializing a non-web application

So far, we've been looking at a web application. But what if you have
a non-web application? How do you start it? The jar command provides a
`--main` option for this case:

    $ bundle exec torquebox jar --main app/init

This results in a jar that, when started, will call `require
"app/init"` after initializing JRuby, which can be used to bootstrap
your application.

[Sinatra]: http://sinatrarb.com/
[Web Guide]: ./file.web.html
