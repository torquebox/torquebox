# Copyright 2008-2012 Red Hat, Inc, and individual contributors.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

require 'fileutils'
require 'tmpdir'
require 'rexml/document'
require 'rubygems'

require 'rubygems/installer'
require 'rubygems/dependency_installer'

class AssemblyTool

  attr_accessor :src_dir

  attr_accessor :base_dir
  attr_accessor :build_dir

  attr_accessor :torquebox_dir
  attr_accessor :gem_repo_dir

  attr_accessor :jboss_dir
  attr_accessor :jruby_dir

  attr_accessor :m2_repo
  
  attr_reader :options

  def modules
    @modules ||= []
  end

  def require_rubygems_indexer
    begin
      gem 'builder', '3.0.0'
    rescue Gem::LoadError=> e
      puts "Installing builder gem"
      require 'rubygems/commands/install_command'
      installer = Gem::Commands::InstallCommand.new
      installer.options[:args] = [ 'builder' ]
      installer.options[:version] = '3.0.0'
      installer.options[:generate_rdoc] = false
      installer.options[:generate_ri] = false
      begin
        installer.execute
      rescue Gem::SystemExitException=>e2
      end
    end
    require 'rubygems/indexer'
  end

  def initialize(options = {}) 
    @options = options

    @src_dir   = File.expand_path( File.dirname(__FILE__) + '/../../..' )
    @base_dir  = File.expand_path( File.dirname(__FILE__) + '/..' )
    @build_dir = @base_dir  + '/target/stage'

    @torquebox_dir = @build_dir  + '/torquebox'
    @gem_repo_dir  = @build_dir  + '/gem-repo'

    @jboss_dir = @torquebox_dir + '/jboss'
    @jruby_dir = @torquebox_dir + '/jruby'

    @m2_repo = nil 
    if ( ENV['M2_REPO'] ) 
      @m2_repo = ENV['M2_REPO']
    else
      @m2_repo = ENV['HOME'] + '/.m2/repository'
    end
  end

  def self.install_gem(gem)
     AssemblyTool.new().install_gem( gem, true )
  end

  def self.copy_gem_to_repo(gem)
    AssemblyTool.new().copy_gem_to_repo( gem, true )
  end

  def unzip(path)
    windows? ? `jar xf #{path}` : `unzip -q #{path}`
  end

  def windows?
    Config::CONFIG['host_os'] =~ /mswin/
  end

  def install_gem(gem, update_index=false)
    puts "Installing #{gem}"
    opts = {
      :bin_dir     => @jruby_dir + '/bin',
      :env_shebang => true,
      :install_dir => @jruby_dir + '/lib/ruby/gems/1.8',
      :wrappers    => true
    }

    installer = Gem::DependencyInstaller.new( opts )
    installer.install( gem )
    copy_gem_to_repo(gem, update_index) if File.exist?( gem )
  end

  def copy_gem_to_repo(gem, update_index=false)
    FileUtils.mkdir_p gem_repo_dir + '/gems'
    FileUtils.cp gem, gem_repo_dir + '/gems'
    update_gem_repo_index if update_index
  end

  def update_gem_repo_index
    puts "Updating index" 
    require_rubygems_indexer()
    opts = {
    }
    indexer = Gem::Indexer.new( gem_repo_dir )
    indexer.generate_index
  end

  def self.install_module(name, path)
     AssemblyTool.new().install_module( name, path )
  end
 
  def install_module(name, path, dest_suffix = nil, remember = true)
    puts "Installing #{name} from #{path}"
    dest_suffix ||= "/modules/org/torquebox/#{name}/main"
    Dir.chdir( @jboss_dir ) do 
      dest_dir = Dir.pwd + dest_suffix
      FileUtils.rm_rf dest_dir
      FileUtils.mkdir_p File.dirname( dest_dir )
      FileUtils.cp_r path, dest_dir
    end
    modules << name if remember
  end

  def install_polyglot_module(name, version)
    artifact_path = "#{m2_repo}/org/projectodd/polyglot-#{name}/#{version}/polyglot-#{name}-#{version}-module.zip"
    artifact_dir = Dir.mktmpdir
    Dir.chdir( artifact_dir ) do
      unzip( artifact_path )
    end
    install_module( name, artifact_dir, "/modules/org/projectodd/polyglot/#{name}/main", false )
  end

  def increase_deployment_timeout(doc)
    if options[:deployment_timeout]
      profiles = doc.root.get_elements( '//profile' )
      profiles.each do |profile|
        subsystem = profile.get_elements( "subsystem[@xmlns='urn:jboss:domain:deployment-scanner:1.1']" ).first
        scanner = subsystem.get_elements( 'deployment-scanner' ).first
        scanner.add_attribute( 'deployment-timeout', options[:deployment_timeout] )
      end
    end
  end

  def add_cache(doc)
    profiles = doc.root.get_elements( '//profile' )
    profiles.each do |profile|
      subsystem = profile.get_elements( "subsystem[@xmlns='urn:jboss:domain:infinispan:1.2']" ).first
      container = subsystem.add_element( 'cache-container', 'name'=>'torquebox', 'default-cache'=>'sessions' )
      cache = container.add_element( 'local-cache', 'name'=>'sessions' )
      cache.add_element( 'eviction', 'strategy'=>'LRU', 'max-entries'=>'10000' )
      cache.add_element( 'expiration', 'max-idle'=>'100000' )
    end
  end

  def add_ha_cache(doc)
    profiles = doc.root.get_elements( '//profile' )
    profiles.each do |profile|
      subsystem = profile.get_elements( "subsystem[@xmlns='urn:jboss:domain:infinispan:1.2']" ).first
      container = subsystem.get_elements( "cache-container[@name='web']" ).first
      #subsystem.get_elements( "cache-container" ).each do |c|
        #puts "CACHE_CONTAINER: #{c.to_s}"
      #end
      container.add_attribute( "aliases", "torquebox standard-session-cache" ) if container
    end
  end

  def add_extensions(doc, extra_extensions)
    extensions = doc.root.get_elements( 'extensions' ).first
    all_modules = modules.map { |name| "org.torquebox.#{name}" }
    all_modules += extra_extensions.map { |name_bits| "org.#{name_bits.join('.')}" } if extra_extensions
    all_modules.each do |name|
      previous_extension = extensions.get_elements( "extension[@module='#{name}']" )
      if ( previous_extension.empty? )
        extensions.add_element( 'extension', 'module'=>"#{name}" )
      end
    end
  end

  def remove_non_web_extensions(doc)
    to_remove = %W(org.jboss.as.clustering.infinispan org.jboss.as.connector
                   org.jboss.as.ejb3 org.jboss.as.jacorb
                   org.jboss.as.jaxrs org.jboss.as.jpa org.jboss.as.messaging
                   org.jboss.as.osgi org.jboss.as.sar
                   org.jboss.as.threads org.jboss.as.webservices
                   org.jboss.as.weld org.torquebox.cdi org.torquebox.jobs
                   org.torquebox.messaging org.torquebox.security org.torquebox.services)
    extensions = doc.root.get_elements( 'extensions' ).first
    to_remove.each do |name|
      extensions.delete_element( "extension[@module='#{name}']" )
    end
  end

  def add_subsystems(doc, extra_subsystems)
    profiles = doc.root.get_elements( '//profile' )
    profiles.each do |profile|
      all_modules = modules.map { |name| "torquebox-#{name}" }
      all_modules += extra_subsystems.map { |_, group, name| "#{group}-#{name}"} if extra_subsystems
      all_modules.each do |name|
        add_subsystem(profile, name)
      end
    end
  end

  def add_subsystem(element, name)
    previous_subsystem = element.get_elements( "subsystem[@xmlns='urn:jboss:domain:#{name}:1.0']" )
    if ( previous_subsystem.empty? )
      #element.add_element( 'subsystem', 'xmlns'=>"urn:jboss:domain:#{name}:1.0" )
      element.add_element( subsystem_element( name ) )
    end
  end
  
  def subsystem_element(name)
    short_name = name.split('-').last
    custom_subsystem_path = base_dir + "/../../modules/#{short_name}/src/subsystem/subsystem.xml"
    if ( ! File.exist?( custom_subsystem_path ) ) 
      e = REXML::Element.new( 'subsystem' )
      e.add_attribute( 'xmlns', "urn:jboss:domain:#{name}:1.0" )
      return e
    end

    custom_doc = REXML::Document.new( File.read( custom_subsystem_path ) )
    custom_doc.root 
  end

  def add_socket_bindings(doc)
    servers = doc.root.get_elements( '//server' ) + doc.root.get_elements( '//domain/socket-binding-groups' )
    servers.each do |server|
      modules.each do |name|
        binding_path = base_dir + "/../../modules/#{name}/src/subsystem/socket-binding.conf"
        if ( File.exists?( binding_path ) )
          group_name, port_name, port = File.read( binding_path ).chomp.split(':')
          binding_group = server.get_elements( "socket-binding-group[@name='#{group_name}']" )
          if ( binding_group.empty? )
            $stderr.puts "invalid binding group #{group_name}"
            next
          end
          previous_binding = binding_group.first.get_elements( "socket-binding[@name='#{port_name}']" )
          if ( previous_binding.empty? )
            binding_group.first.add_element( 'socket-binding', 'name'=>port_name, 'port'=>port )
          end
        end
      end
    end
  end

  def add_messaging_socket_binding(doc)
    groups = doc.root.get_elements( '//server/socket-binding-group' ) + doc.root.get_elements( '//domain/socket-binding-groups/socket-binding-group' )
    groups.each do |group|
      binding = group.get_elements( "*[@name='default-broadcast-group']" )
      if ( binding.empty? )
        group.add_element( 'socket-binding', 
                           'name'=>'default-broadcast-group',
                           'port'=>'0',
                           'multicast-address'=>'231.7.7.7',
                           'multicast-port'=>'9876')
      end
    end
  end

  def remove_non_web_subsystems(doc)
    to_remove = %W(datasources ejb3 infinispan jacorb jaxrs jca jpa messaging osgi
                   resource-adapters sar threads
                   webservices weld torquebox-cdi torquebox-jobs
                   torquebox-messaging torquebox-security torquebox-services)
    profiles = doc.root.get_elements( '//profile' )
    profiles.each do |profile|
      to_remove.each do |name|
        profile.delete_element( "subsystem[@xmlns='urn:jboss:domain:#{name}:1.1']" )
      end
    end
  end

  def set_welcome_root(doc)
    #unless options[:enable_welcome_root].nil?
    #  element = doc.root.get_elements("//virtual-server").first
    #  element.attributes['enable-welcome-root'] = options[:enable_welcome_root]
    #end
    doc.root.get_elements("//virtual-server").each do |e|
      e.attributes.delete('enable-welcome-root')
    end
  end

  def tweak_jboss_web_properties(doc)
    # Ensure cookie paths don't get quoted
    set_system_property(doc, 'org.apache.tomcat.util.http.ServerCookie.FWD_SLASH_IS_SEPARATOR', false)
    # Wait for an available thread instead of dropping new connections
    # when max-threads is reached
    # FIXME: Temporarily disabled because of performance issues
    set_system_property(doc, 'org.apache.tomcat.util.net.WAIT_FOR_THREAD', false)
  end

  def set_system_property(doc, name, value)
    props = doc.root.elements['system-properties'] || REXML::Element.new('system-properties')
    prop = props.elements["property[@name='#{name}']"] || REXML::Element.new('property')
    prop.attributes['name'] = name
    prop.attributes['value'] = value
    props.push(prop) unless prop.parent
    doc.root.insert_after('extensions', props) unless props.parent
  end

  def setup_server_groups(doc)
    doc.root.get_elements( '//server-groups/server-group' ).each &:remove
    server_groups = doc.root.get_elements( '//server-groups' ).first
    server_group = REXML::Element.new( 'server-group' )

    socket_binding_group = REXML::Element.new( 'socket-binding-group' )
    socket_binding_group.attributes['ref'] = 'standard-sockets'
    server_group.add_element( socket_binding_group )

    server_group.attributes['name'] = 'default'
    server_group.attributes['profile'] = 'default'
    server_groups.add_element( server_group )
  end

  def fix_profiles(doc)
    profile = doc.root.get_elements( "//profile[@name='default']" ).first
    profile.remove
    profile = doc.root.get_elements( "//profile[@name='ha']" ).first
    profile.attributes['name'] = 'default' 
  end

  def fix_socket_binding_groups(doc)
    group = doc.root.get_elements( "//socket-binding-group[@name='standard-sockets']" ).first
    group.remove
    group = doc.root.get_elements( "//socket-binding-group[@name='ha-sockets']" ).first
    group.attributes['name'] = 'standard-sockets'
  end

  def remove_destinations(doc)
    destinations = doc.root.get_elements( '//jms-topic' ) + doc.root.get_elements( '//jms-queue' )
    destinations.each &:remove
  end

  def disable_management_security(doc)
    interfaces = doc.root.get_elements("//management-interfaces/*")
    interfaces.each { |i| i.attributes.delete( 'security-realm' )}
  end

  def enable_messaging_jmx(doc)
    hornetq_server = doc.root.get_elements( "//hornetq-server" ).first
    e = REXML::Element.new( 'jmx-management-enabled' )
    e.text = 'true'
    hornetq_server.add_element( e )
  end

  def adjust_messaging_config(doc)
    hornetq_server = doc.root.get_elements( "//subsystem[@xmlns='urn:jboss:domain:messaging:1.1']/hornetq-server" ).first
    address_setting = hornetq_server.get_elements( "address-settings/address-setting" ).first
    address_setting.get_elements( 'address-full-policy' ).first.text = 'PAGE'
    address_setting.get_elements( 'max-size-bytes' ).first.text = '20971520'

    factories = hornetq_server.get_elements( "jms-connection-factories/connection-factory")
    factories.each do |factory|
      e = REXML::Element.new( 'consumer-window-size' )
      e.text = '1'
      factory.add_element( e )
    end
  end

  def remove_messaging_security(doc)
    hornetq_server = doc.root.get_elements( "//subsystem[@xmlns='urn:jboss:domain:messaging:1.1']/hornetq-server" ).first
    e = REXML::Element.new( 'security-enabled' )
    e.text = 'false'
    hornetq_server.add_element( e )
  end

  def fix_messaging_clustering(doc)
    hornetq_server = doc.root.get_elements( "//subsystem[@xmlns='urn:jboss:domain:messaging:1.1']/hornetq-server" ).first

    e = REXML::Element.new( 'cluster-user' )
    e.text = 'admin'
    hornetq_server.add_element( e );

    e = REXML::Element.new( 'cluster-password' )
    e.text = 'password'
    hornetq_server.add_element( e )
  end

  def fix_host_servers(doc)
    doc.root.get_elements( '//servers/server' ).each &:remove
    servers = doc.root.get_elements( '//servers' ).first

    1.upto( 2 ) do |i|
      server = REXML::Element.new( 'server' )
      server.attributes['name'] = sprintf( "server-%02d", i )
      server.attributes['group'] = "default"

      jvm = REXML::Element.new( 'jvm' )
      jvm.attributes['name'] = 'default'

      e = REXML::Element.new( 'heap' )
      e.attributes['size'] = '256m'
      e.attributes['max-size'] = '1024m'
      jvm.add_element( e )

      e = REXML::Element.new( 'permgen' )
      e.attributes['size'] = '256m'
      e.attributes['max-size'] = '512m'
      jvm.add_element( e )

      server.add_element( jvm )

      socket_binding_group = REXML::Element.new( 'socket-bindings' )
      socket_binding_group.attributes['port-offset'] = (i-1) * 100
      server.add_element( socket_binding_group )
     
      servers.add_element( server )
    end
  end

  def add_logger_categories(doc)
    categories = { 'org.jboss.jca.adapters.jdbc.extensions.mysql' => 'ERROR' }
    profiles = doc.root.get_elements( '//profile' )
    profiles.each do |profile|
      subsystem = profile.get_elements( "subsystem[@xmlns='urn:jboss:domain:logging:1.1']" ).first
      categories.each_pair do |category, level|
        container = subsystem.add_element( 'logger', 'category' => category )
        container.add_element( 'level', 'name' => level )
      end
    end
  end

  def adjust_modcluster_config(doc)
    profiles = doc.root.get_elements( '//profile' )
    profiles.each do |profile|

      subsystem = profile.get_elements( "subsystem[@xmlns='urn:jboss:domain:modcluster:1.0']" ).first
      unless subsystem.nil?
        config = subsystem.get_elements( 'mod-cluster-config' ).first
        config.add_attribute( 'excluded-contexts', 'invoker,jbossws,juddi,console' )
      end
    end
  end

  def transform_host_config(input_file, output_file)
    doc = REXML::Document.new( File.read( input_file ) )
    Dir.chdir( @jboss_dir ) do
      fix_host_servers(doc)
      disable_management_security(doc)
      FileUtils.mkdir_p( File.dirname(output_file) )
      open( output_file, 'w' ) do |f|
        doc.write( f, 4 )
      end
    end
  end

  def transform_config(input_file, output_file, options = { })
    domain = options[:domain]
    ha = options[:ha]
    doc = REXML::Document.new( File.read( input_file ) )

    Dir.chdir( @jboss_dir ) do
      
      increase_deployment_timeout(doc) unless domain
      add_extensions(doc, options[:extra_modules])
      add_subsystems(doc, options[:extra_modules])
      ha ? add_ha_cache(doc) : add_cache(doc) # add_cache seems unnecessary here!
      set_welcome_root(doc)
      tweak_jboss_web_properties(doc)
      remove_destinations(doc)
      disable_management_security(doc)

      if ( domain ) 
        setup_server_groups(doc)
        fix_profiles(doc)
        fix_socket_binding_groups(doc)
      end

      add_socket_bindings(doc)

      if ( domain || ha )
        fix_messaging_clustering(doc)
        add_messaging_socket_binding(doc)
        adjust_modcluster_config(doc)
      end

      adjust_messaging_config(doc)
      enable_messaging_jmx(doc)
      remove_messaging_security(doc)

      add_logger_categories(doc)

      # Uncomment to create a minimal standalone.xml
      # remove_non_web_extensions(doc)
      # remove_non_web_subsystems(doc)

      FileUtils.mkdir_p( File.dirname(output_file) )
      open( output_file, 'w' ) do |f|
        doc.write( f, 4 )
      end
    end
  end

  def transform_standalone_conf(torquebox_java_opts)
    conf = File.join( jboss_dir, 'bin', 'standalone.conf')
    unless File.read( conf ).include?('$APPEND_JAVA_OPTS')
      File.open( conf, 'a' ) do |file|
        file.write( %Q(\nJAVA_OPTS="$JAVA_OPTS #{torquebox_java_opts} $APPEND_JAVA_OPTS"\n) )
      end
    end
  end

  def transform_standalone_conf_bat(torquebox_java_opts)
    conf = File.join( jboss_dir, 'bin', 'standalone.conf.bat')
    unless File.read( conf ).include?('%APPEND_JAVA_OPTS%')
      File.open( conf, 'a' ) do |file|
        file.write( %Q(\nset "JAVA_OPTS=%JAVA_OPTS% #{torquebox_java_opts} %APPEND_JAVA_OPTS%"\n) )
      end
    end
  end

end

