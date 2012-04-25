#!/usr/bin/env ruby

require 'java'
require 'rexml/document'

class BuildInfo
  def initialize()
    @versions = Hash.new { |hash, key| hash[key] = { } }
  end

  def determine_torquebox_versions
    version = from_system_property( "version.torquebox" )
    version = 'unknown' if version.nil? || version.empty?

    build_revision = `git rev-parse HEAD`.strip
    git_output = `git status -s`.lines
    build_revision << ' +modifications' if git_output.any? {|line| line =~ /^ M/ }

    @versions['TorqueBox'] = {
      'version' => version,
      'build.revision' => build_revision,
      'build.user' => ENV['USER'],
      'build.number' => ENV['BUILD_NUMBER']
    }
  end

  def m2_repo
    m2_repo = ENV['M2_REPO'] || from_system_property( "maven.repo" ) || (ENV['HOME'] + '/.m2/repository')
    puts "Using maven local repo: " + m2_repo
    m2_repo
  end

  def determine_component_versions
    #it would be nice to include mod_cluster as well
    @versions["JBossAS"]["version"] = from_system_property( "version.jbossas" )
    @versions["Quartz"]["version"] = from_polyglot_properties( "version.org.quartz-scheduler" )
    @versions["JRuby"]["version"] = from_system_property( "version.jruby" )
    @versions['Infinispan']['version'] = org.infinispan.Version::VERSION
    @versions['HornetQ']['version'] = org.hornetq.utils.VersionLoader.version.full_version
  end

  def dump_versions
    path = File.dirname( __FILE__ ) + '/../target/classes/org/torquebox/torquebox.properties'
    File.open( path, 'w' ) do |out|
      @versions.each do |component, data|
        data.each do |key, value|
          out.write("#{component}.#{key}=#{value}\n")
        end
      end
    end
  end

  def from_system_property(selector)
    java.lang.System.getProperty( selector )
  end

  def from_polyglot_properties(name)
    unless @polyglot_props
      pg_version = from_system_property( "version.polyglot" )
      require File.join( m2_repo, "org/projectodd/polyglot-core/#{pg_version}/polyglot-core-#{pg_version}.jar" )
      @polyglot_props = java.util.Properties.new
      @polyglot_props.load( org.projectodd.polyglot.core.ProjectInfo.java_class.class_loader.getResourceAsStream( "org/projectodd/polyglot/polyglot.properties" ) )
      end
    @polyglot_props.get_property( name )
  end

  def go!()
    determine_torquebox_versions
    determine_component_versions
    dump_versions
  end
end

BuildInfo.new.go!
