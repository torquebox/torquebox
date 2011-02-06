class FormHandlingController < ApplicationController

  def index
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
