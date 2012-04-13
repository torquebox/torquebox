#!/usr/bin/env ruby

require 'rubygems'
require 'fileutils'

class RailsTemplateAdjuster

  attr_accessor :build_number

  def initialize( version, build_number )
    @version      = version
    @build_number = build_number
    @incremental = @version =~ /\-SNAPSHOT$/
    @version.gsub!(/\-SNAPSHOT$/, '')

  end
   
  def adjust
    original = template_path
    modified = original + '.modified'
    FileUtils.rm( modified, :force => true )
    FileUtils.cp( original, modified )
    File.open( original, 'r' ) do |input|
      File.open( modified, 'w' ) do |output|
        output.puts generate_source
        input.each_line { |line| write_line( output, line ) }
      end
    end
    FileUtils.rm original
    FileUtils.mv( modified, original )
  end
   
  private

  def local_build?
    @build_number.nil? || @build_number == '' || @build_number == "${env.BUILD_NUMBER}"
  end

  def write_line( output, line )
    if line =~ /gem [\'\"](torquebox.*?)[\'\"]/
      output.puts "gem \'#{$1}\', \'#{@version}\'"
    else
      output.puts line unless line =~ /^add_source \"http\:\/\/torquebox.org\/.+?\/builds\/[0-9]+\/gem-repo\"$/
    end
  end
  
  def generate_source 
    if local_build?
      "# Local build - no source needed"
    elsif @incremental
      "add_source \"http://torquebox.org/2x/builds/#{@build_number}/gem-repo\""
    else
      ""
    end
  end
   
  def template_path
    File.dirname( __FILE__ ) + '/../target/stage/torquebox/share/rails/template.rb'
  end

end

RailsTemplateAdjuster.new( ARGV[0], ARGV[1] ).adjust
