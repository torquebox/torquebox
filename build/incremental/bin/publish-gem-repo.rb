#!/usr/bin/env ruby

$: << File.dirname( __FILE__ ) + '/../lib'
require 'dav'
require 'find'
require 'pathname'

class Uploader

  BASE_URL = 'https://repository-torquebox.forge.cloudbees.com/incremental/gem-repo/'

  attr_accessor :build_number
  attr_accessor :repo_dir

  def initialize(credentials_path, build_number, repo_dir)
    @build_number = build_number
    @repo_dir     = repo_dir
    @dav          = DAV.new( credentials_path )
  end

  def upload()
    puts "Upload #{build_number} from #{repo_dir}"
    @dav.mkcol( BASE_URL )
    repo_url = BASE_URL + "/#{build_number}"
    @dav.mkcol( repo_url )
    
    Dir.chdir( repo_dir ) do
      Find.find( '.' ) do |entry|   
        path = Pathname.new( entry ).cleanpath
        begin
          if ( File.directory?( entry ) ) 
            @dav.mkcol( repo_url + "/#{path}" ) unless entry == '.'
          else
            @dav.put( repo_url + "/#{path}", entry )
          end
        rescue => e
          puts e
          puts e.backtrace
        end
      end
    end
  end

end

Uploader.new( ARGV[0], ARGV[1], ARGV[2] ).upload



