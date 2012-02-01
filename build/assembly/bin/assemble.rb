#!/usr/bin/env ruby

$: << File.dirname( __FILE__ ) + '/../lib'

require 'assembly_tool'
require 'fileutils'
require 'rexml/document'
require 'rbconfig'

class Assembler 

  attr_accessor :tool
  attr_accessor :jboss_zip
  attr_accessor :jruby_zip

  attr_accessor :torquebox_version
  attr_accessor :jboss_version
  attr_accessor :jruby_version
  attr_accessor :polyglot_version
  attr_accessor :stilts_version

  attr_accessor :m2_repo
  attr_accessor :config_stash
  
  def initialize() 
    @tool = AssemblyTool.new(:deployment_timeout => 1200, :enable_welcome_root => false)
    determine_versions

    @m2_repo   = @tool.m2_repo

    puts "Maven repo: #{@m2_repo}"
    @jboss_zip = @m2_repo + "/org/jboss/as/jboss-as-dist/#{@jboss_version}/jboss-as-dist-#{@jboss_version}.zip"
    @jruby_zip = @m2_repo + "/org/jruby/jruby-dist/#{@jruby_version}/jruby-dist-#{@jruby_version}-bin.zip"

    @config_stash = File.dirname(__FILE__) + '/../target'
  end

  def determine_versions
    doc = REXML::Document.new( File.read( tool.base_dir + '/../../pom.xml' ) )
    @torquebox_version = doc.get_elements( "project/version" ).first.text
    @jboss_version     = doc.get_elements( "project/properties/version.jbossas" ).first.text
    @jruby_version     = doc.get_elements( "project/properties/version.jruby" ).first.text
    @polyglot_version  = doc.get_elements( "project/properties/version.polyglot" ).first.text
    @stilts_version    = doc.get_elements( "project/properties/version.org.projectodd.stilts" ).first.text
    puts "TorqueBox.... #{@torquebox_version}" 
    puts "JBoss........ #{@jboss_version}"
    puts "JRuby........ #{@jruby_version}"
    puts "Polyglot..... #{@polyglot_version}"
    puts "Stilts....... #{@stilts_version}"
    #puts doc
  end

  def clean()
    FileUtils.rm_rf   tool.build_dir
  end

  def prepare()
    FileUtils.mkdir_p tool.torquebox_dir
    FileUtils.mkdir_p tool.gem_repo_dir
  end

  def lay_down_jboss
    if File.exist?( tool.jboss_dir ) 
      #puts "JBoss already laid down"
    else
      puts "Laying down JBoss"
      Dir.chdir( File.dirname( tool.jboss_dir ) ) do
        tool.unzip( jboss_zip )
        original_dir= File.expand_path( Dir[ 'jboss-*' ].first )
        FileUtils.mv original_dir, tool.jboss_dir
      end
    end
  end

  def lay_down_jruby
    if ( File.exist?( tool.jruby_dir ) )
      #puts "JRuby already laid down" 
    else
      puts "Laying down JRuby" 
      Dir.chdir( File.dirname( tool.jruby_dir ) ) do
        tool.unzip( jruby_zip )
        original_dir = File.expand_path( Dir[ 'jruby-*' ].first )
        FileUtils.mv original_dir, tool.jruby_dir

        ### HACK begin
        ###
        ### JRuby 1.6.6 suffers from a StackOverflowError in some cases
        ### and this hack (see http://jira.codehaus.org/browse/JRUBY-6407)
        ### moves a require() statement around to avoid the aforementioned
        ### StackOverflowError
        ###
        ### Hopefully remove this when using > JRuby 1.6.6
        ###
        open( tool.jruby_dir + '/lib/ruby/site_ruby/1.8/rubygems/defaults/jruby.rb', 'r' ) do |original_rb|
          open( tool.jruby_dir + '/lib/ruby/site_ruby/1.8/rubygems/defaults/jruby.rb.new', 'w' ) do |new_rb|
            new_rb.puts( "require 'jruby/util'" )
            original_rb.each do |line|
              if ( line =~ %r(jruby/util) ) 
                puts "skip #{line}"
              else
                puts line
                new_rb.puts line
              end
            end
          end
        end
        FileUtils.mv tool.jruby_dir + '/lib/ruby/site_ruby/1.8/rubygems/defaults/jruby.rb.new', tool.jruby_dir + '/lib/ruby/site_ruby/1.8/rubygems/defaults/jruby.rb'
        ###
        ### HACK end

      end
    end
  end

  def polyglot_modules
    @polyglot_modules ||= ['hasingleton']
  end
  
  def install_modules
    modules = Dir[ tool.base_dir + '/../../modules/*/target/*-module' ].map do |module_dir|
      [ File.basename( module_dir, '-module' ).gsub( /torquebox-/, '' ), module_dir ]
    end

    # Ensure core module is first-ish
    modules.unshift( modules.assoc( "core" ) ).uniq!

    # Ensure boot module is first
    modules.unshift( modules.assoc( "bootstrap" ) ).uniq!

    modules.each do |module_name, module_dir|
      tool.install_module( module_name, module_dir )
    end

    polyglot_modules.each do |name|
      tool.install_polyglot_module( name, polyglot_version )
    end
  end

  def install_gems
    # Install gems in order specified by modules in gems/pom.xml
    gem_pom = REXML::Document.new( File.read( tool.base_dir + '/../../gems/pom.xml' ) )
    gem_dirs = gem_pom.get_elements( "project/modules/module" ).map { |m| m.text }

    gem_dirs.each do |gem_dir|
      Dir[ tool.base_dir + '/../../gems/' + gem_dir + '/target/*.gem' ].each do |gem_package|
        puts "Install gem: #{gem_package}"
        tool.install_gem( gem_package )
      end
    end

    # Additionally install rack and bundler gems
    tool.install_gem( 'rack' )
    tool.install_gem( 'bundler' )
    tool.update_gem_repo_index
  end

  def install_share
    # torquebox-rake-support gem needs this
    puts "Installing share"

    FileUtils.cp File.join( tool.src_dir, 'gems', 'rake-support', 'share', 'Rakefile' ), tool.torquebox_dir

    init_dir  = FileUtils.mkdir_p( File.join( tool.torquebox_dir, 'share', 'init' ) )
    rails_dir = FileUtils.mkdir_p( File.join( tool.torquebox_dir, 'share', 'rails' ) )
    js_dir    = FileUtils.mkdir_p( File.join( tool.torquebox_dir, 'share', 'javascript' ) )

    FileUtils.cp( File.join( tool.src_dir, 'gems', 'rake-support', 'share', 'init', 'torquebox.conf' ), init_dir )
    FileUtils.cp( File.join( tool.src_dir, 'gems', 'rake-support', 'share', 'init', 'torquebox.conf.erb' ), init_dir )
    FileUtils.cp( File.join( tool.src_dir, 'gems', 'rake-support', 'share', 'init', 'TorqueBoxAgent.plist.template' ), init_dir )
    FileUtils.cp( File.join( tool.src_dir, 'gems', 'rake-support', 'share', 'rails', 'template.rb' ), rails_dir )
    FileUtils.cp( File.join( tool.src_dir, 'gems', 'rake-support', 'share', 'rails', 'openshift_app_builder.rb' ), rails_dir )

    stomp_js = @m2_repo + "/org/projectodd/stilts/stilts-stomp-client-js/#{@stilts_version}/stilts-stomp-client-js-#{@stilts_version}.js"

    FileUtils.cp( stomp_js, File.join( js_dir, 'stilts-stomp.js' ) )
  end

  def stash_stock_configs
    FileUtils.cp( tool.jboss_dir + '/standalone/configuration/standalone-full.xml',
                  config_stash + '/standalone-full.xml' ) unless File.exist?( config_stash + '/standalone-full.xml' )
    FileUtils.cp( tool.jboss_dir + '/standalone/configuration/standalone-ha.xml',
                  config_stash + '/standalone-ha.xml' ) unless File.exist?( config_stash + '/standalone-ha.xml' )
    FileUtils.cp( tool.jboss_dir + '/domain/configuration/domain.xml',
                  config_stash + '/domain.xml' )     unless File.exist?( config_stash + '/domain.xml' )
  end

  def stash_stock_host_config
    FileUtils.cp( tool.jboss_dir + '/domain/configuration/host.xml',
                  config_stash + '/host.xml' ) unless File.exist?( config_stash + '/host.xml' )
  end

  def trash_stock_host_config
    FileUtils.rm_f( tool.jboss_dir + '/domain/configuration/host.xml' )
  end

  def trash_stock_configs
    FileUtils.rm_f( Dir[ tool.jboss_dir + '/standalone/configuration/standalone*.xml' ] )
    FileUtils.rm_f( Dir[ tool.jboss_dir + '/domain/configuration/domain*.xml' ] )
  end

  def transform_configs
    stash_stock_configs
    trash_stock_configs
    polyglot_mods = polyglot_modules.map { |name| ["projectodd", "polyglot", name]}
    tool.transform_config(config_stash + '/standalone-full.xml',
                          'standalone/configuration/standalone-full.xml',
                          :extra_modules => polyglot_mods)
    tool.transform_config(config_stash + '/standalone-ha.xml',
                          'standalone/configuration/standalone-ha.xml',
                          :extra_modules => polyglot_mods,
                          :ha => true )
    tool.transform_config(config_stash + '/domain.xml',
                          'domain/configuration/domain.xml',
                          :extra_modules => polyglot_mods,
                          :domain => true,
                          :ha => true )
  end

  def transform_host_config
    stash_stock_host_config
    trash_stock_host_config
    tool.transform_host_config( config_stash + '/host.xml', 'domain/configuration/host.xml' )
  end

  def assemble() 
    #clean
    prepare
    lay_down_jruby
    lay_down_jboss
    install_modules
    install_gems
    install_share
    transform_configs
    transform_host_config
    Dir.chdir( tool.jboss_dir ) do
      FileUtils.cp( 'standalone/configuration/standalone-full.xml', 'standalone/configuration/standalone.xml' )
    end
  end
end

if __FILE__ == $0 || '-e' == $0 # -e == called from mvn
  Assembler.new.assemble
end

