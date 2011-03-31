require 'erb'
require 'yaml'

class RackApp
  def call(env)
   request = Rack::Request.new(env)
 
    result = [  
      200, {'Content-Type' => 'text/html'}, 
      "<div id='success'>INITIAL</div>",
    ]

    version_yml = YAML.load( File.read( 'version.yml' ) )
    version = version_yml['version']
    version_yml['version'] = version + 1
    File.open( 'version.yml', 'w' ) do |f|
      f.write YAML.dump( version_yml )
    end
    
    template = ERB.new( File.read( 'rack_app.rb.erb' ) )
    puts "rewriting app"
    File.open( 'rack_app.rb', 'w' ) do |f|
      f.write template.result(binding)
    end
  
    result
  end
end 
