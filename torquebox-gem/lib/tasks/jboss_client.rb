
client_dir = RAILS_ROOT + "/client"
client_lib_dir = client_dir + "/lib"
boot_script_file = client_dir + "/boot.rb"

namespace :jboss do
  namespace :client do

    CORE_CLIENT_JARS = [
      "jboss-rails.jar"
    ]

    DEPENDENCY_CLIENT_JARS = [
      "jboss-messaging-client.jar",
      "jboss-javaee.jar",
      "jboss-logging-spi.jar",
      "jboss-aop-client.jar",
      "jboss-remoting.jar",
      "jboss-common-core.jar",
      "jboss-mdr.jar",
      "jnp-client.jar",
      "trove.jar",
      "javassist.jar",
      "concurrent.jar",
      "log4j.jar",
      #"jboss-client.jar",
    ]

    directory client_lib_dir
    directory client_lib_dir + '/core'
    directory client_lib_dir + '/dependencies'

    desc "Set up support for client applications"
    task :setup=>[ :client_jars, boot_script_file ] 

    task :client_jars=>[ :core_client_jars, :dependency_client_jars ]

    task :core_client_jars=>[ client_lib_dir + '/core' ] 

    task :dependency_client_jars=>[ client_lib_dir + '/dependencies' ]

  end
end


file boot_script_file=>[ File.dirname(__FILE__) + '/boot.rb.in' ]  do 
  File.open( boot_script_file, 'w' ) do |f|
    f.write( File.read( File.dirname(__FILE__) + '/boot.rb.in' ) )
  end
  puts "Created boot script"
end

CORE_CLIENT_JARS.each do |jar|
  jar_path = "#{client_lib_dir}/core/#{jar}"
  desc "Install #{jar_path}"
  file jar_path=>[ JBoss::RakeUtils.deployer ] do
    Dir.chdir client_dir do
      `jar xvf #{JBoss::RakeUtils.deployer} lib/core/#{jar}`
    end
    FileUtils.touch( jar_path )
    puts "Installed #{jar}"
  end
  task 'jboss:client:core_client_jars'=>[ jar_path ]
end

DEPENDENCY_CLIENT_JARS.each do |jar|
  jar_path = "#{client_lib_dir}/dependencies/#{jar}"
  desc "Install #{jar_path}"
  file jar_path=>[ "#{JBoss::RakeUtils.jboss_home}/client/#{jar}" ] do 
    FileUtils.cp( "#{JBoss::RakeUtils.jboss_home}/client/#{jar}", client_lib_dir + "/dependencies" )
    puts "Installed #{jar}"
  end
  task 'jboss:client:dependency_client_jars'=>[ jar_path ]
end

