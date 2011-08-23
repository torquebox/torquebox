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
      file_contents = params[:upload_file].read
      @data = "#{file_contents} As returned."
      @path = File.join( Dir.tmpdir, "file_#{Time.now.to_f}" )
      File.open( @path, 'wb' ) { |f| f.write( file_contents ) }
    end
  end

end
