#!/usr/bin/env ruby

require 'rexml/document'

class BuildInfo
  def initialize()
  end

  def determine_versions
    path = File.dirname( __FILE__ ) + '/../../../pom.xml' 
    doc = REXML::Document.new( File.read( path ) )

    @version  = doc.get_elements( "project/version" ).first.text
    @build_revision = `git rev-parse HEAD`.strip

    git_output = `git status -s`.lines
    @build_revision += git_output.any? {|line| line =~ /^ M/ } ? ' +modifications' : ''
    @build_user = ENV['USER']
  end

  def dump_properties
    path = File.dirname( __FILE__ ) + '/../target/classes/org/torquebox/torquebox.properties'
    File.open( path, 'w' ) do |file|
      file.puts "version: #{@version}"
      file.puts "build.revision: #{@build_revision}"
      file.puts "build.user: #{@build_user}"
      file.puts "build.number: #{ENV['BUILD_NUMBER']}"
    end
  end

  def go!()
    determine_versions
    dump_properties
  end
end

BuildInfo.new.go!
