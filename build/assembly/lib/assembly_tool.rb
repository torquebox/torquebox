require 'fileutils'
require 'rexml/document'

class AssemblyTool
  def initialize() 
    @base_dir  = File.expand_path( File.dirname(__FILE__) + '/..' )

    @jboss_zip = @base_dir + '/zipfiles/jboss-7.0.x.zip'
    @jruby_zip = @base_dir + '/zipfiles/jruby-bin-1.6.1.zip'

    @build_dir = @base_dir  + '/target/stage'
    @jboss_dir = @build_dir + '/jboss-as'
    @jruby_dir = @build_dir + '/jruby'
  end

  def self.install_module(name, path)
     AssemblyTool.new().install_module( name, path )
  end
 
  def install_module(name, path)
    puts "installing #{name} from #{path} into #{@jboss_dir}"
    Dir.chdir( @jboss_dir ) do 
      dest_dir = Dir.pwd + "/modules/org/torquebox/#{name}/main"
      puts "dest: #{dest_dir}"
      FileUtils.rm_rf dest_dir
      FileUtils.mkdir_p File.dirname( dest_dir )
      puts "copy from: #{path}"
      FileUtils.cp_r path, dest_dir
    end
    add_extension( name ) 
    add_subsystem( name ) 
  end

  def add_extension(name)
    Dir.chdir( @jboss_dir ) do
      doc = REXML::Document.new( File.read( 'standalone/configuration/standalone.xml' ) )

      extensions = doc.root.get_elements( 'extensions' ).first
      previous_extension = extensions.get_elements( "extension[@module='org.torquebox.#{name}']" )
      if ( previous_extension.empty? )
        extensions.add_element( 'extension', 'module'=>"org.torquebox.#{name}" )
      end

      open( 'standalone/configuration/standalone.xml', 'w' ) do |f|
        doc.write( f, 4 )
      end
    end
  end

  def add_subsystem(name)
    Dir.chdir( @jboss_dir ) do
      doc = REXML::Document.new( File.read( 'standalone/configuration/standalone.xml' ) )

      profile = doc.root.get_elements( 'profile' ).first
      previous_subsystem = profile.get_elements( "subsystem[@xmlns='urn:jboss:domain:torquebox-#{name}:1.0']" )
  
      if ( previous_subsystem.empty? )
        profile.add_element( 'subsystem', 'xmlns'=>"urn:jboss:domain:torquebox-#{name}:1.0" )
      end
  
      open( 'standalone/configuration/standalone.xml', 'w' ) do |f|
        doc.write( f, 4 )
      end
    end

  end

end


