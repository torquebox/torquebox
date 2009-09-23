
$: << File.dirname(__FILE__) + '/lib'

require 'torquebox-vfs'

jar_path  = "#{Dir.pwd}/target/dependencies/jboss-vfs.jar"
file_path = "/META-INF/maven/org.jboss/jboss-vfs/pom.properties" 
full_path = "vfszip://#{jar_path}#{file_path}"

#File.open( full_path, 'r' ) do |f|
  #puts f.read
#end

#f = File.open( full_path, 'r' )
#puts f.read

#puts File.read( full_path )
#puts File.mtime( full_path )


#files = Dir.glob( "vfszip://#{jar_path}/org/jboss/virtual/**/*Handler*" )
#files = Dir[ "vfszip://#{jar_path}/org/jboss/virtual/**/*Handler*" ]

#files.each do |f|
#  puts f
#end

Dir.open( "vfszip://#{jar_path}/org/jboss/virtual" ) do |dir|
  while ( ( c = dir.read ) != nil )
    puts c
  end
end


#puts dir.path

#dir.each do |child|
  #puts child
#end
