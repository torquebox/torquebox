class WidgetsController < ApplicationController
  def index
    Widget.new.foo(1)
    Widget.new.foo(2)
    Widget.new.foo(3)
  end
end
