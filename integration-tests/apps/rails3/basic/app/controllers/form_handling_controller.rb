class FormHandlingController < ApplicationController

  def index
    # Just to ensure we trigger a session to be created for this user
    session[:foo] = 'bar'
    if ( params[:value].nil? || params[:value] == '' ) 
      @value = ''
    else
      @value = "#{params[:value]} is returned"
    end
  end

  def upload_file
    if ( request.put? )
      @data = "#{params[:upload_file].read} As returned."
    end
  end

end
