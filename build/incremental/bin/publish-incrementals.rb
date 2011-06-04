#!/usr/bin/env ruby

$: << File.dirname( __FILE__ ) + '/../lib'
require 'dav'
require 'find'
require 'pathname'

class Publisher

  BASE_URL = 'https://repository-torquebox.forge.cloudbees.com/incremental'

  attr_accessor :build_number

  def initialize(credentials_path, build_number)
    @build_number = build_number
    @dav          = DAV.new( credentials_path )
  end

  def build_base_url
    BASE_URL + "/#{build_number}"
  end

  def latest_base_url
    BASE_URL + "/LATEST"
  end

  def build_gem_repo_url
    "#{build_base_url}/gem-repo"
  end

  def latest_gem_repo_url
    "#{latest_base_url}/gem-repo"
  end

  def dav_mkdir_p(url)
    puts "mkdir #{url}"
    @dav.mkcol( url )
  end

  def dav_put(url, file)
    puts "put #{url}"
    @dav.put( url, file )
  end

  def dav_put_r(root_url, root_dir)
    Dir.chdir( root_dir ) do 
      Find.find( '.' ) do |entry|
        if ( entry == '.' )
          next
        end

        if ( File.directory?( entry ) )
          dav_mkdir_p( root_url + '/' + Pathname( entry ).cleanpath )
        else
          dav_put( root_url + '/' + Pathname( entry ).cleanpath, entry )
        end
      end
    end
  end

  def publish_all()
    dav_mkdir_p( build_base_url )
    dav_mkdir_p( latest_base_url )
    publish_distribution()
    publish_documentation()
    publish_gem_repo()
  end

  def html_docs_path()
    File.dirname(__FILE__) + '/../../../docs/en-US/target/docbook/publish/en-US/xhtml/'
  end

  def pdf_doc_path()
    File.dirname(__FILE__) + '/../../../docs/en-US/target/docbook/publish/en-US/pdf/torquebox-docs-en_US.pdf'
  end

  def epub_path()
    Dir[ ( ENV['M2_REPO'] || ( ENV['HOME'] + '/.m2/repository' ) ) + '/org/torquebox/torquebox-docs-en_US/*/torquebox-docs-en_US-*.epub' ].first
  end

  def dist_path()
    File.dirname(__FILE__) + '/../../dist/target/torquebox-dist-bin.zip'
  end

  def json_metadata_path()
    File.dirname(__FILE__) + '/../../dist/target/build-metadata.json'
  end

  def gem_repo_path()
    File.dirname(__FILE__) + '/../../assembly/target/stage/gem-repo'
  end

  def publish_documentation()
    dav_put( build_base_url + '/torquebox-docs.epub', epub_path )
    dav_put( latest_base_url + '/torquebox-docs.epub', epub_path )

    dav_put( build_base_url + '/torquebox-docs.pdf', pdf_doc_path )
    dav_put( latest_base_url + '/torquebox-docs.pdf', pdf_doc_path )


    dav_mkdir_p( build_base_url + '/html-docs' )
    dav_mkdir_p( latest_base_url + '/html-docs' )

    dav_put_r( build_base_url + '/html-docs', html_docs_path )
    dav_put_r( latest_base_url + '/html-docs', html_docs_path )
  end

  def publish_distribution()
    dav_put( build_base_url + "/#{File.basename( json_metadata_path ) }", json_metadata_path )
    dav_put( latest_base_url + "/#{File.basename( json_metadata_path ) }", json_metadata_path )

    dav_put( build_base_url  + "/#{File.basename( dist_path ) }", dist_path )
    dav_put( latest_base_url + "/#{File.basename( dist_path ) }", dist_path )
  end

  def publish_gem_repo()
    dav_mkdir_p( build_gem_repo_url )
    dav_mkdir_p( latest_gem_repo_url )
    dav_put_r( build_gem_repo_url, gem_repo_path )
    dav_put_r( latest_gem_repo_url, gem_repo_path )
  end

end

Publisher.new( ARGV[0], ARGV[1] ).publish_all



