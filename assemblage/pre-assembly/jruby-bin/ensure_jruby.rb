
require 'fileutils'

jruby_bin = File.join( MAVEN_REPO, 'org', 'jruby', 'jruby-bin', JRUBY_VERSION, "jruby-bin-#{JRUBY_VERSION}.zip" )

if ( File.exists?( jruby_bin ) )
  puts "jruby-bin-#{JRUBY_VERSION} in place"
else
  if ( ! File.exists?( './target/jruby-bin.zip' ) )
    puts "grabbing #{JRUBY_BIN_URL}"
    FileUtils.mkdir_p( './target' )
    puts `curl --silent #{JRUBY_BIN_URL} -o ./target/jruby-bin.zip`
  end
  puts "installing ./target/jruby-bin.zip"
  cmd = "mvn install:install-file -Dfile=./target/jruby-bin.zip -DgroupId=org.jruby -DartifactId=jruby-bin -Dpackaging=zip -Dversion=#{JRUBY_VERSION}"
  puts `#{cmd}`
end
