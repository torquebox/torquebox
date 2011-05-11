#!/usr/bin/env ruby

require 'fileutils'
require 'rexml/document'

class Assembler 
  def initialize() 
    @base_dir  = File.expand_path( File.dirname(__FILE__) )

    @jboss_zip = @base_dir + '/zipfiles/jboss-7.0.x.zip'
    @jruby_zip = @base_dir + '/zipfiles/jruby-bin-1.6.1.zip'

    @build_dir = @base_dir  + '/target/stage'
    @jboss_dir = @build_dir + '/jboss-as'
    @jruby_dir = @build_dir + '/jruby'
  end

  def clean()
    FileUtils.rm_rf   @build_dir
  end

  def prepare()
    FileUtils.mkdir_p @build_dir
  end

  def lay_down_jboss
    Dir.chdir( @build_dir ) do 
      puts `unzip -q #{@jboss_zip}`
      original_dir= File.expand_path( Dir[ 'jboss-*' ].first )
      FileUtils.mv original_dir, @jboss_dir
    end
  end

  def lay_down_jruby
    Dir.chdir( @build_dir ) do
      puts `unzip -q #{@jruby_zip}`
      original_dir= File.expand_path( Dir[ 'jruby-*' ].first )
      FileUtils.mv original_dir, @jruby_dir
    end
  end

  def install_module(name)
    Dir.chdir( @jboss_dir ) do 
      dest_dir = Dir.pwd + "/modules/org/torquebox/#{name}/main"
      FileUtils.mkdir_p dest_dir
      FileUtils.cp Dir[@base_dir + "/../../modules/#{name}/target/torquebox-#{name}-module/*"], dest_dir
    end
    add_extension( name ) 
  end

  def add_extension(name)
    Dir.chdir( @jboss_dir ) do
      doc = REXML::Document.new( File.read( 'standalone/configuration/standalone.xml' ) )
      extensions = doc.root.get_elements( 'extensions' ).first
      extensions.add_element( 'extension', 'module'=>"org.torquebox.#{name}" )
      profile = doc.root.get_elements( 'profile' ).first
      profile.add_element( 'subsystem', 'xmlns'=>"urn:jboss:domain:torquebox-#{name}:1.0" )
      open( 'standalone/configuration/standalone.xml', 'w' ) do |f|
        doc.write( f, 4 )
      end
    end
  end

  def assemble() 
    clean()
    prepare()
    lay_down_jruby()
    lay_down_jboss()
    install_module( :core )
    install_module( :web )
  end
end

Assembler.new.assemble

