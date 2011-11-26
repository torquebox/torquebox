
require 'jruby/profiler'

class TestController < ApplicationController

  def index
    t = TestProfile.new
    profile_data = t.profile

    profile_printer = JRuby::Profiler::GraphProfilePrinter.new profile_data
    baos = java.io.ByteArrayOutputStream.new
    profile_printer.printProfile(java.io.PrintStream.new( baos ))
    render :text => baos.toString
  end

end

class TestProfile

  def profile

   JRuby::Profiler.profile do
     puts "hello world"
   end

  end

end
