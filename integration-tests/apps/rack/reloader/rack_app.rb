require 'erb'
require 'yaml'

class RackApp
  def call(env)
   request = Rack::Request.new(env)

    result = [
      200, {'Content-Type' => 'text/html'},
      "<div id='success'>INITIAL</div>",
    ]

    version = env['QUERY_STRING']

    template = ERB.new( File.read( 'rack_app.rb.erb' ) )
    puts "rewriting app"
    File.open( 'rack_app.rb', 'w' ) do |f|
      f.write template.result(binding)
    end
    template = ERB.new( File.read( 'simple_service.rb.erb' ) )
    File.open( 'simple_service.rb', 'w' ) do |f|
      f.write template.result(binding)
    end

    result
  end
end
