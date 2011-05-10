
class WebsocketsClientGenerator < Rails::Generators::Base
  
  source_root File.expand_path('../templates', __FILE__) 
  
  def generate
    puts "generating ws client"
    copy_file "torquebox_ws_client.js", "public/javascripts/torquebox_ws_client.js"
    puts "done generating ws client"
  end
   
end  