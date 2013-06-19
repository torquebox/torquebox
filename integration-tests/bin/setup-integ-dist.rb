
require 'java'
require 'fileutils'
require 'rexml/document'

assembly_dir = File.expand_path( ARGV[0] )
overlay_dir  = File.expand_path( ARGV[1] )
output_dir   = File.expand_path( ARGV[2] )
SLIM_DISTRO = java.lang::System.getProperty( "org.torquebox.slim_distro" ) == "true"

def unzip(path)
  if windows?
    `jar.exe xf #{path}`
  else
    `jar xf #{path}`
  end
end

def windows?
  RbConfig::CONFIG['host_os'] =~ /mswin/
end

if SLIM_DISTRO
  puts "assembly from... #{assembly_dir}"
  puts "output to....... #{output_dir}"

  jboss_dir = "#{output_dir}/jboss"

  Dir.chdir( assembly_dir ) do
    if ( java.lang::System.getProperty( 'os.name' ) =~ /.*windows.*/i )
      puts "*** Syncing to integ-dist by copy"
      if ( File.exists?( output_dir ) )
        puts "*** Only copying torquebox modules"
        modules_path = 'jboss/modules/system/layers/torquebox/org/torquebox'
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
              '--include jboss/domain',
              '--exclude jruby/share/ri',
              '--exclude jruby/lib/ruby/gems/1.8/doc',
              output_dir ].join( ' ' )
      puts cmd
      puts `#{cmd}`
    end
  end
else
  eap_version = java.lang::System.getProperty( 'version.eap' )
  puts "Overlaying TorqueBox on top of EAP #{eap_version}"

  eap_zip = "jboss-eap-#{eap_version}.zip"
  eap_zip_path = File.join( File.dirname( __FILE__), '..', eap_zip )
  unless File.exists?( eap_zip_path )
    $stderr.puts ""
    $stderr.puts "Before you can run integration tests against JBoss EAP you\n" +
      "must download #{eap_zip} from http://www.jboss.org/jbossas/downloads\n" +
      "and place it in the integration-tests/ directory."
    $stderr.puts ""
    exit 1
  end

  FileUtils.mkdir_p( File.dirname( output_dir ) )
  FileUtils.rm_rf( output_dir )
  FileUtils.mkdir( output_dir )

  Dir.chdir( output_dir ) do
    unzip( eap_zip_path )
  end

  jboss_dir = Dir.glob( File.join( output_dir , 'jboss-eap-6*' ) ).first
  FileUtils.cp_r( "#{overlay_dir}/.", jboss_dir )

  unless windows?
    FileUtils.chmod( 0755, "#{jboss_dir}/bin/standalone.sh" )
    FileUtils.chmod( 0755, "#{jboss_dir}/bin/domain.sh" )
  end
end

categories = ["org.torquebox", "TorqueBox", "org.jboss.security", "org.projectodd"]
config_files = ["#{jboss_dir}/standalone/configuration/standalone.xml",
                "#{jboss_dir}/standalone/configuration/torquebox-full.xml",
                "#{jboss_dir}/domain/configuration/domain.xml",
                "#{jboss_dir}/domain/configuration/torquebox-full.xml"]

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

standalone_xmls = ["#{jboss_dir}/standalone/configuration/standalone.xml",
                   "#{jboss_dir}/standalone/configuration/torquebox-full.xml"]
standalone_xmls.each do |standalone_xml|
  doc = REXML::Document.new(File.read(standalone_xml))
  # Configure a test JAAS login module
  puts "Adding test security domain to #{standalone_xml}"
  domain_set = doc.root.get_elements("//security-domains").first
  domain = domain_set.add_element("security-domain", "name"=>"pork", "cache-type"=>"none")
  auth = domain.add_element("authentication")
  login_module = auth.add_element("login-module", "code"=>"UsersRoles", "flag"=>"required")
  login_module.add_element("module-option", "name"=>"usersProperties", "value"=>"${jboss.server.config.dir}/pork-users.properties")
  login_module.add_element("module-option", "name"=>"rolesProperties", "value"=>"${jboss.server.config.dir}/pork-roles.properties")

  puts "Disabling standalone management interface security"
  # Disable management interface security
  interfaces = doc.root.get_elements("//management-interfaces/*")
  interfaces.each { |i| i.attributes.delete( 'security-realm' )}

  open(standalone_xml, 'w') do |file|
    doc.write(file, 4)
  end
end

host_xml = "#{jboss_dir}/domain/configuration/host.xml"
doc = REXML::Document.new(File.read(host_xml))
puts "Disabling domain management interface security"
# Disable management interface security
interfaces = doc.root.get_elements("//management-interfaces/*")
interfaces.each { |i| i.attributes.delete( 'security-realm' )}
open(host_xml, 'w') do |file|
  doc.write(file, 4)
end

# Write the users and roles properties files
File.open("#{jboss_dir}/standalone/configuration/pork-users.properties", File::CREAT|File::TRUNC|File::RDWR, 0644) do |f|
  f.write("crunchy=bacon\n")
end
File.open("#{jboss_dir}/standalone/configuration/pork-roles.properties", File::CREAT|File::TRUNC|File::RDWR, 0644) do |f|
  f.write("crunchy=carnivore\n")
end

# Remove stale Gemfile.lock files
puts "Removing Gemfile.lock files..."
FileUtils.rm_rf( Dir["#{File.dirname(__FILE__)}/../apps/**/Gemfile.lock"] )

puts "Removing old log files..."
FileUtils.rm_rf( Dir["#{jboss_dir}/standalone/log/*"] )
FileUtils.rm_rf( Dir["#{jboss_dir}/standalone/deployments/*"] )
FileUtils.rm_rf( Dir["#{jboss_dir}/domain/log/*"] )
FileUtils.rm_rf( Dir["#{jboss_dir}/domain/servers/server-01/log/*"] )
FileUtils.rm_rf( Dir["#{jboss_dir}/domain/servers/server-02/log/*"] )
