#!/usr/bin/env ruby

$: << File.dirname( __FILE__ ) + '/../lib'

require 'assembly_tool'
require 'fileutils'
require 'rexml/document'

class Assembler 
  def initialize() 
    @tool = AssemblyTool.new
    determine_versions

    #@base_dir  = File.expand_path( File.dirname(__FILE__) )

    @m2_repo   = ENV['HOME'] + '/.m2/repository'
    @jboss_zip = @m2_repo + "/org/jboss/as/jboss-as-build/#{@jboss_version}/jboss-as-build-#{@jboss_version}.zip"
    @jruby_zip = @m2_repo + "/org/jruby/jruby-dist/#{@jruby_version}/jruby-dist-#{@jruby_version}-bin.zip"

    #puts "JBoss bundle: #{@jboss_zip}"
    #puts "JRuby bundle: #{@jruby_zip}"

    @build_dir  = @tool.base_dir  + '/target/stage'
    @jboss_dir = @build_dir + '/jboss-as'
    @jruby_dir = @build_dir + '/jruby'
  end

  def determine_versions
    doc = REXML::Document.new( File.read( @tool.base_dir + '/../../parent/pom.xml' ) )
    @jboss_version = doc.get_elements( "project/properties/version.jbossas" ).first.text
    @jruby_version = doc.get_elements( "project/properties/version.jruby" ).first.text
    puts "JBoss version: #{@jboss_version}" 
    puts "JRuby version: #{@jruby_version}"
    #puts doc
  end

  def clean()
    FileUtils.rm_rf   @build_dir
  end

  def prepare()
    FileUtils.mkdir_p @build_dir
  end

  def lay_down_jboss
    if File.exist?( @jboss_dir ) 
      #puts "JBoss already laid down"
    else
      puts "Laying down JBoss"
      Dir.chdir( @build_dir ) do 
        `unzip -q #{@jboss_zip}`
        original_dir= File.expand_path( Dir[ 'jboss-*' ].first )
        FileUtils.mv original_dir, @jboss_dir
      end
    end
  end

  def lay_down_jruby
    if ( File.exist?( @jruby_dir ) )
      #puts "JRuby already laid down" 
    else
      puts "Laying down JRuby" 
      Dir.chdir( @build_dir ) do
        `unzip -q #{@jruby_zip}`
        original_dir= File.expand_path( Dir[ 'jruby-*' ].first )
        FileUtils.mv original_dir, @jruby_dir
      end
    end
  end

  def install_modules
    Dir[ @tool.base_dir + '/../../modules/*/target/*-module/' ].each do |module_dir|
      module_name = File.basename( module_dir, '-module' ).gsub( /torquebox-/, '' )
      #puts "Install module: #{module_name}"
      @tool.install_module( module_name, module_dir )
    end
  end

  def install_gems
    Dir[ @tool.base_dir + '/../../gems/*/target/*.gem' ].each do |gem_package|
      puts "Install gem: #{gem_package}"
      @tool.install_gem( gem_package )
    end
  end

  def assemble() 
    #clean
    prepare
    lay_down_jruby
    lay_down_jboss
    install_modules
    install_gems
  end
end

Assembler.new.assemble

