
class RootController < ApplicationController

  def injectiontest
    puts "About to call thing_one()"
    @use_me = thing_one()
    puts "Called thing_one() -> #{@use_me} #{@use_me.class} #{@use_me.java_class.name}"
  end

  def streaming
    render :stream => true
  end

end
