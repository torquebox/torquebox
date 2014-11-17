# @title Web Guide

# Web

TorqueBox supports any Rack-based web applications. This includes
Rails, Sinatra, and many other frameworks that build on top of Rack.

The most common web server use cases can be handled with the
`torquebox run`, `rackup`, and `rails s torquebox` commands. But, for
advanced usage, the API for creating and managing web servers resides
in the [Web module](TorqueBox/Web.html).

## The gem

Web services are provided by the `torquebox-web` gem, and can be used
independently of other TorqueBox services.
