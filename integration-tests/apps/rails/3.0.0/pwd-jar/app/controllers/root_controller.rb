
require 'weird/uuid-3.2.jar'

java_import 'com.eaio.uuid.UUID' 

class RootController < ApplicationController

  def index
    @uuid = UUID.new 
  end

end
