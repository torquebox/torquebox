
java_import 'com.fasterxml.uuid.Generators'

class RootController < ApplicationController

  def index
    @uuid = Generators.random_based_generator.generate
  end

end
