#!/usr/bin/env jruby

require 'rubygems'
require 'mongrel'

class SimpleHandler < Mongrel::HttpHandler
  def process(request, response)
    puts "request=#{request.inspect}"
    response.start(200) do |head,out|
      head["Content-Type"] = "text/plain"
      out.write("hello!\n")
    end
  end
end

class Mongrel::HttpServer
  attr_reader :socket
end

h = Mongrel::HttpServer.new("0.0.0.0", 8042 )
h.register("/", SimpleHandler.new)
acceptor = h.run
acceptor.join

