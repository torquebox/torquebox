#!/usr/bin/env ruby

require 'rubygems'
$: << File.dirname( __FILE__ ) + '/../lib'
require 'dav'
require 'find'
require 'pathname'
require 'json'
require 'rexml/document'

class Cleaner

  BASE_URL = 'https://repository-torquebox.forge.cloudbees.com'
  COLLECTION = '/upstream/'

  def initialize(username, password, keep)
    @dav  = DAV.new( username, password )
    @keep = keep.to_i
  end

  def clean()
    puts find_artifact_dirs( BASE_URL + COLLECTION ).inspect
  end

  def find_artifact_dirs(url)
    entries = listing( url )
    if ( artifact_dir?( entries ) )
      purge_old( entries )
    else
      entries.collect do |entry|
        find_artifact_dirs( BASE_URL + entry.path )
      end
    end
  end

  def purge_old(entries)
    entries.select{|e| 
      r = false
      if ( e.to_s =~ %r(.*.incremental.([0-9]+)/$) )
        v = $1
        r = ( v.to_i < @keep )
      end
      r
    }.each do |e|
      @dav.delete( BASE_URL + e.to_s )
    end
  end

  def artifact_dir?(entries)
    entries.any?{|e| 
      ( e.to_s =~ /.*maven-metadata.xml$/ )
    }
  end

  def listing(url)
    doc = REXML::Document.new( @dav.propfind( url )[2] )
    entries = doc.root.get_elements( "//D:response" )
    children = []
    entries[1..-1].each do |response|
      is_dir = ! response.get_elements( "D:propstat/D:prop/lp1:resourcetype/D:collection" ).empty?
      children << DavThing.new( response.get_elements( 'D:href' )[0].text, is_dir )
    end
    children
  end

  class DavThing
    attr_accessor :path

    def initialize(path, is_dir)
      @path = path
      @is_dir = is_dir
    end

    def dir?()
      @is_dir
    end

    def to_s
      @path
    end

  end


end

# username, password, minimum_incr_to_keep

Cleaner.new( ARGV[0], ARGV[1], ARGV[2] ).clean



