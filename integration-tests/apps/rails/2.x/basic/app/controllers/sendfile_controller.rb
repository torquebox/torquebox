class SendfileController < ApplicationController

  def index
    send_file "#{File.dirname(__FILE__)}/sendfile.dat"
  end

end
