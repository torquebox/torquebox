
require 'java'
require 'fileutils'
require 'rexml/document'

assembly_dir = File.expand_path( ARGV[0] )
output_dir   = File.expand_path( ARGV[1] )

puts "assembly from... #{assembly_dir}"
puts "output to....... #{output_dir}"

Dir.chdir( assembly_dir ) do
  if ( java.lang::System.getProperty( 'os.name' ) =~ /.*windows.*/i )
    puts "*** Syncing to integ-dist by copy"
    if ( File.exists?( output_dir ) )
      puts "*** Only copying torquebox modules"
      modules_path = 'jboss/modules/org/torquebox'
      FileUtils.rm_rf( "#{output_dir}/#{modules_path}" )
      FileUtils.mkdir_p( File.dirname( "#{output_dir}/#{modules_path}" ) )
      FileUtils.cp_r( "./#{modules_path}", "#{output_dir}/#{modules_path}" )
    else
      FileUtils.mkdir_p( File.dirname( output_dir ) )
      FileUtils.rm_rf( output_dir )
      FileUtils.cp_r( '.', output_dir )
    end
  else
    puts "*** Syncing to integ-dist by rsync"
    FileUtils.mkdir_p( File.dirname( output_dir ) )
    cmd = [ 'rsync -av . --relative',
            '--include jboss/modules',
            '--include jboss/standalone',
            '--exclude jruby/share/ri',
            '--exclude jruby/lib/ruby/gems/1.8/doc',
            output_dir ].join( ' ' )
    puts cmd
    puts `#{cmd}`
  end

  categories = ["org.torquebox", "TorqueBox", "org.jboss.security", "org.projectodd"]
  config_files = ["#{output_dir}/jboss/standalone/configuration/standalone.xml",
                  "#{output_dir}/jboss/domain/configuration/domain.xml"]

  config_files.each do |config_file|
    puts "Adding trace log level for #{categories.join(", ")} categories to #{config_file} file"
    doc = REXML::Document.new(File.read(config_file))
    profiles = doc.root.get_elements("//profile")
    profiles.each do |profile|
      logging_subsystem = profile.get_elements("subsystem[contains(@xmlns, 'urn:jboss:domain:logging:')]").first
      categories.each do |category|
        logger = logging_subsystem.add_element("logger", "category" => category)
        logger.add_element("level", "name" => "TRACE")
      end
    end
    open(config_file, 'w') do |file|
      doc.write(file, 4)
    end
  end

  standalone_xml = "#{output_dir}/jboss/standalone/configuration/standalone.xml"
  doc = REXML::Document.new(File.read(standalone_xml))
  # Configure a test JAAS login module
  puts "Adding test security domain to #{standalone_xml}"
  domain_set = doc.root.get_elements("//security-domains").first
  domain = domain_set.add_element("security-domain", "name"=>"pork", "cache-type"=>"none")
  auth = domain.add_element("authentication")
  login_module = auth.add_element("login-module", "code"=>"UsersRoles", "flag"=>"required")
  login_module.add_element("module-option", "name"=>"usersProperties", "value"=>"${jboss.server.config.dir}/pork-users.properties")
  login_module.add_element("module-option", "name"=>"rolesProperties", "value"=>"${jboss.server.config.dir}/pork-roles.properties")

  # Write the users and roles properties files
  File.open("#{output_dir}/jboss/standalone/configuration/pork-users.properties", File::CREAT|File::TRUNC|File::RDWR, 0644) do |f|
    f.write("crunchy=bacon\n")
  end
  File.open("#{output_dir}/jboss/standalone/configuration/pork-roles.properties", File::CREAT|File::TRUNC|File::RDWR, 0644) do |f|
    f.write("crunchy=carnivore\n")
  end

  open(standalone_xml, 'w') do |file|
    doc.write(file, 4)
  end
end

# Remove stale Gemfile.lock files
puts "Removing Gemfile.lock files..."
FileUtils.rm_rf( Dir["#{File.dirname(__FILE__)}/../apps/**/Gemfile.lock"] )

puts "Removing old log files..."
FileUtils.rm_rf( Dir["#{output_dir}/jboss/standalone/log/*"] )
FileUtils.rm_rf( Dir["#{output_dir}/jboss/standalone/deployments/*"] )
FileUtils.rm_rf( Dir["#{output_dir}/jboss/domain/servers/server-01/log/*"] )
FileUtils.rm_rf( Dir["#{output_dir}/jboss/domain/servers/server-02/log/*"] )
