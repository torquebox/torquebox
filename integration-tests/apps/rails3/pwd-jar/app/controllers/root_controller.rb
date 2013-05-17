
require 'weird/java-uuid-generator-3.1.3.jar'

java_import 'com.fasterxml.uuid.Generators'

class RootController < ApplicationController

  def index
    @uuid = Generators.random_based_generator.generate
  end

end
