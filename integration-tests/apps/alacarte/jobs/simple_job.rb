
class SimpleJob 

  def run() 
    $stderr.puts "Job executing!"
    touchfile = ENV['BASEDIR'] + '/target/touchfile.txt'
    File.open( touchfile, 'w' ) do |f|
      f.puts( "Updated #{Time.now}" )
    end
  end

end
