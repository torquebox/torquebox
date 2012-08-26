#!/usr/bin/env jruby
#
# Overlays WFK TorqueBox on top of EAP 6.
# ARGV[0] should be the path to the unzipped EAP 6 distribution.
#

require 'fileutils'
require 'rbconfig'
require 'rexml/document'
require 'rubygems'
require 'rubygems/dependency_installer'

class EapOverlayer
  def initialize(eap_dir)
    if eap_dir.nil? || eap_dir == '-h' || eap_dir == '--help'
      print_usage_and_exit(0)
    end
    @eap_dir = File.expand_path(eap_dir)
    unless valid_eap_dir?
      $stderr.puts "ERROR: #{eap_dir} is not a valid EAP 6 distribution"
      print_usage_and_exit(1)
    end
    @torquebox_dir = File.expand_path(File.dirname(__FILE__))
  end

  def overlay
    puts "Overlaying #{@torquebox_dir} onto #{@eap_dir}"
    copy_modules
    install_gems
    transform_configs
  end

  def copy_modules
    FileUtils.cp_r(wfk_modules_dir, @eap_dir)
    puts "Copied TorqueBox modules"
  end

  def install_gems
    opts = {
      :bin_dir     => RbConfig::CONFIG['bindir'],
      :env_shebang => true,
      :install_dir => Gem.dir,
      :wrappers    => true
    }

    installer = Gem::DependencyInstaller.new(opts)
    gems_to_install = Dir[File.join(wfk_gem_repo, 'torquebox-*.gem')]
    until gems_to_install.empty?
      gem = gems_to_install.shift
      begin
        installer.install(gem)
        puts "Installed #{File.basename(gem)}"
      rescue Gem::DependencyError => e
        if e.message =~ /requires torquebox-/
          gems_to_install.push(gem)
        else
          raise e
        end
      end
    end
  end

  def transform_configs
    transform_config('standalone/configuration/standalone-full.xml')
    transform_config('standalone/configuration/standalone-full-ha.xml')
  end

  def transform_config(config)
    config_path = File.join(@eap_dir, config)
    puts "Transforming #{config_path}"
    doc = REXML::Document.new(File.read(config_path))
    add_extensions(doc)
    add_subsystems(doc)
    add_cache(doc)
    add_socket_bindings(doc)
    open(config_path, 'w') do |file|
      doc.write(file, 4)
    end
  end

  def add_extensions(doc)
    extensions = doc.root.get_elements('extensions').first
    org_dir = File.join(@torquebox_dir, 'modules', 'org')
    all_extensions = polyglot_modules.map do |module_name|
      "org.projectodd.polyglot.#{module_name}"
    end
    all_extensions += torquebox_modules.map do |module_name|
      "org.torquebox.#{module_name}"
    end
    all_extensions.each do |name|
      previous_extension = extensions.get_elements("extension[@module='#{name}']")
      if ( previous_extension.empty? )
        extensions.add_element('extension', 'module' => "#{name}")
      end
    end

  end

  def add_subsystems(doc)
    profiles = doc.root.get_elements('//profile')
    profiles.each do |profile|
      all_subsystems = polyglot_modules.map do |module_name|
        "polyglot-#{module_name}"
      end
      all_subsystems += torquebox_modules.map do |module_name|
        "torquebox-#{module_name}"
      end
      all_subsystems.each do |name|
        previous_subsystem = profile.get_elements("subsystem[contains(@xmlns, 'urn:jboss:domain:#{name}:')]")
        if previous_subsystem.empty?
          subsystem = { 'xmlns' => "urn:jboss:domain:#{name}:1.0" }
          subsystem['socket-binding'] = 'stomp' if name == 'torquebox-stomp'
          profile.add_element('subsystem', subsystem)
        end
      end
    end
  end

  def add_cache(doc)
    profiles = doc.root.get_elements('//profile')
    profiles.each do |profile|
      subsystem = profile.get_elements("subsystem[contains(@xmlns, 'urn:jboss:domain:infinispan:')]").first
      container = subsystem.get_elements("cache-container[@name='web']").first
      if container # HA config
        default = container.get_elements("replicated-cache[@name='repl']").first
        default.add_attribute("start", "EAGER") if default
        container.add_attribute("aliases", "polyglot torquebox standard-session-cache")
      else
        container = subsystem.get_elements("cache-container[@name='polyglot']").first
        unless container
          container = subsystem.add_element('cache-container', 'name' => 'polyglot',
                                            'default-cache' => 'sessions', 'aliases' => 'torquebox')
          cache = container.add_element('local-cache', 'name' => 'sessions', 'start' => 'EAGER')
          cache.add_element('eviction', 'strategy' => 'LRU', 'max-entries' => '10000')
          cache.add_element('expiration', 'max-idle' => '100000')
          cache.add_element('transaction', 'mode' => "FULL_XA")
        end
      end
    end
  end

  def add_socket_bindings(doc)
    servers = doc.root.get_elements('//server') + doc.root.get_elements('//domain/socket-binding-groups')
    servers.each do |server|
      binding_group = server.get_elements("socket-binding-group[@name='standard-sockets']")
      unless binding_group.empty?
        previous_binding = binding_group.first.get_elements("socket-binding[@name='stomp']")
        if previous_binding.empty?
          binding_group.first.add_element('socket-binding', 'name' => 'stomp', 'port' => 8675)
        end
      end
    end
  end

  def torquebox_modules
    org_dir = File.join(@torquebox_dir, 'modules', 'org')
    torquebox_dir = File.join(org_dir, 'torquebox', '*')
    modules = Dir[torquebox_dir].map { |dir| File.basename(dir) }
    modules.unshift('core').uniq! # Ensure core is second
    modules.unshift('bootstrap').uniq! # Ensure bootstrap is first
  end

  def polyglot_modules
    org_dir = File.join(@torquebox_dir, 'modules', 'org')
    polyglot_dir = File.join(org_dir, 'projectodd', 'polyglot', '*')
    Dir[polyglot_dir].map { |dir| File.basename(dir) }
  end

  def wfk_modules_dir
    File.join(@torquebox_dir, 'modules')
  end

  def wfk_gem_repo
    File.join(@torquebox_dir, 'torquebox-rubygems-repo', 'gems')
  end

  def print_usage_and_exit(code)
    puts <<-EOS
Usage: #{$0} eap_directory

Overlay this WFK TorqueBox distribution on top of an existing EAP 6
distribution. Note that this will modify the standalone and domain
configuration files of the target EAP 6 distribition.

EOS
    exit(code)
  end

  def valid_eap_dir?
    index_html = File.join(@eap_dir, 'welcome-content', 'index.html')
    File.exists?(index_html) && File.read(index_html) =~ /EAP 6/
  end
end

if __FILE__ == $0
  EapOverlayer.new(ARGV[0]).overlay
end
