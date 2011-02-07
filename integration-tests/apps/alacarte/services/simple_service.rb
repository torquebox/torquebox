
class SimpleService 

  def start() 
    @should_run = true
    spawn_thread()
  end

  def spawn_thread()
    @thread = Thread.new do
      while @should_run 
        loop_once
        sleep( 1 )
      end
    end
  end

  def loop_once
    $stderr.puts "Service executing loop!"
    touchfile = ENV['BASEDIR'] + '/target/touchfile.txt'
    File.open( touchfile, 'w' ) do |f|
      f.puts( "Updated #{Time.now}" )
    end
  end

  def stop()
    @should_run = false
    @thread.join
  end

end
