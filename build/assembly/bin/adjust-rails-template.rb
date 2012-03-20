#!/usr/bin/env ruby

require 'rubygems'
require 'fileutils'

class RailsTemplateAdjuster

  attr_accessor :build_number

  def initialize(version, build_number)
    @version      = version
    @build_number = build_number
  end
   
  def adjust
    original = template_path
    modified = original + '.modified'
    FileUtils.rm( modified, :force => true )
    FileUtils.cp( original, modified )
    File.open( original, 'r' ) do |input|
      File.open( modified, 'w' ) do |output|
        output.puts generate_source
        input.each_line do |line|
          if line =~ /gem [\'\"](torquebox.*?)[\'\"]/
            output.puts "gem \'#{$1}\', \'#{@version}\'"
          else
            output.puts line unless line =~ /^add_source \"http\:\/\/torquebox.org\/.+?\/builds\/[0-9]+\/gem-repo\"$/
          end
        end
      end
    end
    FileUtils.rm original
    FileUtils.mv( modified, original )
  end
   
  private
  
  def generate_source 
    "add_source \"http://torquebox.org/2x/builds/#{@build_number}/gem-repo\""
  end
   
  def template_path
    File.dirname( __FILE__ ) + '/../target/stage/torquebox/share/rails/template.rb'
  end

end

RailsTemplateAdjuster.new( ARGV[0], ARGV[1] ).adjust
