# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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
  end

  def self.install_gem(gem)
     AssemblyTool.new().install_gem( gem, true )
  end

  def self.copy_gem_to_repo(gem)
    AssemblyTool.new().copy_gem_to_repo( gem, true )
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
 
  def install_module(name, path)
    puts "Installing #{name} from #{path}"
    Dir.chdir( @jboss_dir ) do 
      dest_dir = Dir.pwd + "/modules/org/torquebox/#{name}/main"
      FileUtils.rm_rf dest_dir
      FileUtils.mkdir_p File.dirname( dest_dir )
      FileUtils.cp_r path, dest_dir
    end
    modules << name
  end

  def increase_deployment_timeout(doc)
    if options[:deployment_timeout]
      profiles = doc.root.get_elements( '//profile' )
      profiles.each do |profile|
        subsystem = profile.get_elements( "subsystem[@xmlns='urn:jboss:domain:deployment-scanner:1.1']" ).first
        unless subsystem.nil?
          scanner = subsystem.get_elements( 'deployment-scanner' ).first
          scanner.add_attribute( 'deployment-timeout', options[:deployment_timeout] )
        end
      end
    end
  end

  def add_extensions(doc)
    extensions = doc.root.get_elements( 'extensions' ).first
    modules.each do |name|
      previous_extension = extensions.get_elements( "extension[@module='org.torquebox.#{name}']" )
      if ( previous_extension.empty? )
        extensions.add_element( 'extension', 'module'=>"org.torquebox.#{name}" )
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

  def add_subsystems(doc)
    profiles = doc.root.get_elements( '//profile' )
    profiles.each do |profile|
      modules.each do |name|
        previous_subsystem = profile.get_elements( "subsystem[@xmlns='urn:jboss:domain:torquebox-#{name}:1.0']" )
        if ( previous_subsystem.empty? )
          #profile.add_element( 'subsystem', 'xmlns'=>"urn:jboss:domain:torquebox-#{name}:1.0" )
          profile.add_element( subsystem_element( name ) )
        end
      end
    end
  end

  def subsystem_element(name)
    custom_subsystem_path = base_dir + "/../../modules/#{name}/src/subsystem/subsystem.xml"
    if ( ! File.exist?( custom_subsystem_path ) ) 
      e = REXML::Element.new( 'subsystem' )
      e.add_attribute( 'xmlns', "urn:jboss:domain:torquebox-#{name}:1.0" )
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
    groups = doc.root.get_elements( '//server' ) + doc.root.get_elements( '//domain/socket-binding-groups/socket-binding-group' )
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

  def unquote_cookie_path(doc)
    set_system_property(doc, 'org.apache.tomcat.util.http.ServerCookie.FWD_SLASH_IS_SEPARATOR', false)
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

  def enable_messaging_jmx(doc)
    hornetq_server = doc.root.get_elements( "//hornetq-server" ).first
    e = REXML::Element.new( 'jmx-management-enabled' )
    e.text = 'true'
    hornetq_server.add_element( e )
  end

  def fix_messaging_clustering(doc)
    hornetq_server = doc.root.get_elements( "//subsystem[@xmlns='urn:jboss:domain:messaging:1.1']/hornetq-server" ).first

    e = REXML::Element.new( 'clustered' )
    e.text = 'true'
    hornetq_server.add_element( e )

    e = REXML::Element.new( 'cluster-user' )
    e.text = 'admin'
    hornetq_server.add_element( e );

    e = REXML::Element.new( 'cluster-password' )
    e.text = 'password'
    hornetq_server.add_element( e )

    broadcast_groups = REXML::Element.new( 'broadcast-groups' )
    broadcast_group = REXML::Element.new( 'broadcast-group' )
    broadcast_group.attributes['name'] = 'default-broadcast-group'

    e = REXML::Element.new( 'group-address' )
    e.text = '231.7.7.7' 
    broadcast_group.add_element( e )

    e = REXML::Element.new( 'group-port' )
    e.text = '9876'
    broadcast_group.add_element( e )

    e = REXML::Element.new( 'broadcast-period' )
    e.text = '100'
    broadcast_group.add_element( e )

    e = REXML::Element.new( 'connector-ref' )
    e.text = 'netty'
    broadcast_group.add_element( e )
    broadcast_groups.add_element( broadcast_group )
    hornetq_server.add_element( broadcast_groups )

    discovery_groups = REXML::Element.new( 'discovery-groups' )
    discovery_group = REXML::Element.new( 'discovery-group' )
    discovery_group.attributes['name'] = 'default-discovery-group'

    e = REXML::Element.new( 'group-address' )
    e.text = '231.7.7.7' 
    discovery_group.add_element( e )

    e = REXML::Element.new( 'group-port' )
    e.text = '9876'
    discovery_group.add_element( e )

    e = REXML::Element.new( 'refresh-timeout' )
    e.text = '20000'
    discovery_group.add_element( e )

    discovery_groups.add_element( discovery_group )
    hornetq_server.add_element( discovery_groups )

    cluster_connections = REXML::Element.new( 'cluster-connections' )
    cluster_connection = REXML::Element.new( 'cluster-connection' )
    cluster_connection.attributes['name'] = 'default-cluster-connection'

    e = REXML::Element.new( 'address' )
    e.text = 'jms'
    cluster_connection.add_element( e )

    e = REXML::Element.new( 'connector-ref' )
    e.text = 'netty'
    cluster_connection.add_element( e )

    e = REXML::Element.new( 'retry-interval' )
    e.text = '500'
    cluster_connection.add_element( e )

    e = REXML::Element.new( 'forward-when-no-consumers' )
    e.text = 'true'
    cluster_connection.add_element( e )

    e = REXML::Element.new( 'discovery-group-ref' )
    e.attributes['discovery-group-name'] = 'default-discovery-group'
    cluster_connection.add_element( e )

    cluster_connections.add_element( cluster_connection )
    hornetq_server.add_element( cluster_connections )
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

      socket_binding_group = REXML::Element.new( 'socket-binding-group' )
      socket_binding_group.attributes['ref'] = 'standard-sockets'
      socket_binding_group.attributes['port-offset'] = (i-1) * 100
      server.add_element( socket_binding_group )
     
      servers.add_element( server )
    end
  end

  def transform_host_config(input_file, output_file)
    doc = REXML::Document.new( File.read( input_file ) )
    Dir.chdir( @jboss_dir ) do
      fix_host_servers(doc)
      FileUtils.mkdir_p( File.dirname(output_file) )
      open( output_file, 'w' ) do |f|
        doc.write( f, 4 )
      end
    end
  end

  def transform_config(input_file, output_file, domain=false, ha=false)
    doc = REXML::Document.new( File.read( input_file ) )

    Dir.chdir( @jboss_dir ) do
      
      increase_deployment_timeout(doc)
      add_extensions(doc)
      add_subsystems(doc)
      set_welcome_root(doc)
      unquote_cookie_path(doc)
      remove_destinations(doc)


      if ( domain ) 
        setup_server_groups(doc)
        fix_profiles(doc)
        fix_socket_binding_groups(doc)
      end

      add_socket_bindings(doc)

      if ( domain || ha )
        fix_messaging_clustering(doc)
        add_messaging_socket_binding(doc)
      end

      enable_messaging_jmx(doc)

      # Uncomment to create a minimal standalone.xml
      # remove_non_web_extensions(doc)
      # remove_non_web_subsystems(doc)

      FileUtils.mkdir_p( File.dirname(output_file) )
      open( output_file, 'w' ) do |f|
        doc.write( f, 4 )
      end
    end
  end

end

