require 'fileutils'
require 'rexml/document'
require 'rubygems'

require 'rubygems/installer'

=begin
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
=end

class AssemblyTool

  attr_accessor :src_dir

  attr_accessor :base_dir
  attr_accessor :build_dir

  attr_accessor :torquebox_dir
  attr_accessor :gem_repo_dir

  attr_accessor :jboss_dir
  attr_accessor :jruby_dir

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

  def initialize() 
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
  
  def install_gem(gem, update_index=false)
    puts "Installing #{gem}"
    if ( File.exist?( gem ) ) 
      opts = {
        :install_dir => @jruby_dir + '/lib/ruby/gems/1.8',
        :wrapper     => true
      }
      installer = Gem::Installer.new( gem, opts )
      installer.install
 
      FileUtils.mkdir_p gem_repo_dir + '/gems'
      FileUtils.cp gem, gem_repo_dir + '/gems'
      update_gem_repo_index if update_index
    end
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
    add_extension( name ) 
    add_subsystem( name ) 
  end

  def modify_standalone_xml
    Dir.chdir( @jboss_dir ) do
      doc = REXML::Document.new( File.read( 'standalone/configuration/standalone.xml' ) )

      yield doc
      
      open( 'standalone/configuration/standalone.xml', 'w' ) do |f|
        doc.write( f, 4 )
      end
    end
  end
  
  def add_extension(name)
    modify_standalone_xml do |doc|
      extensions = doc.root.get_elements( 'extensions' ).first
      previous_extension = extensions.get_elements( "extension[@module='org.torquebox.#{name}']" )
      if ( previous_extension.empty? )
        extensions.add_element( 'extension', 'module'=>"org.torquebox.#{name}" )
      end
    end
  end

  def add_subsystem(name)
    modify_standalone_xml do |doc|
      profile = doc.root.get_elements( 'profile' ).first
      previous_subsystem = profile.get_elements( "subsystem[@xmlns='urn:jboss:domain:torquebox-#{name}:1.0']" )
  
      if ( previous_subsystem.empty? )
        profile.add_element( 'subsystem', 'xmlns'=>"urn:jboss:domain:torquebox-#{name}:1.0" )
      end
    end

  end

end


