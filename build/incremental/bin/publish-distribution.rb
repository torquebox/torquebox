#!/usr/bin/env ruby

$: << File.dirname( __FILE__ ) + '/../lib'
require 'dav'
require 'find'
require 'pathname'

class Uploader

  BASE_URL = 'https://repository-torquebox.forge.cloudbees.com/incremental/dist/'

  attr_accessor :build_number
  attr_accessor :dist_path

  def initialize(credentials_path, build_number, dist_path)
    @build_number = build_number
    @dist_path    = dist_path
    @dav          = DAV.new( credentials_path )
  end

  def upload()
    puts "Upload #{build_number} from #{dist_path}"
    @dav.mkcol( BASE_URL )
    repo_url = BASE_URL + "/#{build_number}"

    @dav.mkcol( repo_url )
    @dav.put( repo_url + "/#{File.basename( dist_path ) }", dist_path )

    @dav.mkcol( BASE_URL + "/LATEST/" )
    @dav.put( BASE_URL + "/LATEST/#{File.basename( dist_path ) }", dist_path )
  end

end

Uploader.new( ARGV[0], ARGV[1], ARGV[2] ).upload



