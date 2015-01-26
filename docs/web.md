# @title Web Guide

# Web

TorqueBox supports any Rack-based web applications. This includes
Rails, Sinatra, and many other frameworks that build on top of Rack.

## The gem

Web support is provided by the `torquebox-web` gem, and can be used
independently of other TorqueBox services.


## Running a Rack application via the command line

From inside your Rack application's root directory:
    
    $ torquebox run

If you're using Rails, ensure `torquebox` is in your `Gemfile` and
then you can alternatively run:

    $ rails s torquebox

Of course `rackup` works as well if you prefer for your Rack apps:

    $ rackup -s torquebox


## Running a Rack application via the API

The entry point for exploring the web API is our [TorqueBox::Web
documentation](TorqueBox/Web.html).

    require 'torquebox-web'
    TorqueBox::Web.run(:host => '0.0.0.0', :port => 8080)

For the complete list of options, refer to [TorqueBox::Web.run
documentation](TorqueBox/Web.html#run-class_method).
