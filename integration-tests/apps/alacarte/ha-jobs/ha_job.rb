require 'torquebox-messaging'
require 'torquebox-core'

class HaJob 

  include TorqueBox::Injectors 

  def run() 
    $stderr.puts "Job executing!"
    basedir = ENV['BASEDIR']
    basedir = basedir.gsub( %r(\\:), ':' )
    basedir.gsub!( %r(\\\\), '\\' )
    touchfile = File.join( basedir,  'target', 'hajobs-touchfile.txt' )
      File.open( touchfile, 'w' ) do |f|
        f.puts( "Updated #{Time.now}" )
    end

    queue = inject('/queue/backchannel')
    queue.publish(touchfile)
  end
  
end
