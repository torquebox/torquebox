class WidgetsController < ApplicationController
  def index
    Widget.new.foo
    Widget.new.foo
    Widget.new.foo
  end
end
