#!/usr/bin/env ruby

require 'rubygems'
$: << File.dirname( __FILE__ ) + '/../lib'
require 'dav'
require 'find'
require 'pathname'
require 'json'

class Publisher

  BASE_URL = 'https://repository-projectodd.forge.cloudbees.com/incremental/torquebox'

  attr_accessor :build_number

  def initialize(credentials_path, build_number)
    @build_number = build_number
    @dav          = DAV.new( credentials_path )
    @published_artifacts = []
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

  def dav_put(url, file, remember = true)
    puts "put #{url}"
    @dav.put( url, file )
    @published_artifacts << url if remember
  end

  def dav_rm_rf(url)
    @dav.delete( url )
  end

  def dav_remote_cp_r(src, dest)
    puts @dav.copy( src + '/', dest + '/', :infinity ).inspect
  end

  def dav_put_r(root_url, root_dir)
    Dir.chdir( root_dir ) do 
      Find.find( '.' ) do |entry|
        if ( entry == '.' )
          next
        end

        if ( File.directory?( entry ) )
          dav_mkdir_p( root_url + '/' + Pathname( entry ).cleanpath.to_s )
        else
          dav_put( root_url + '/' + Pathname( entry ).cleanpath.to_s, entry, false )
        end
      end
      @published_artifacts << root_url
    end
    
  end

  def publish_all()
    dav_mkdir_p( build_base_url )
    publish_distribution()
    publish_documentation()
    publish_gem_repo()
    publish_artifact_list()
    
    copy_to_latest()
  end

  def copy_to_latest()
    dav_remote_cp_r( build_base_url, latest_base_url )
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

  def yardocs_path
    File.dirname(__FILE__) + '/../../../gems/target/yardocs'
  end
  
  def javadocs_path()
    File.dirname(__FILE__) + '/../../../target/site/apidocs'
  end

  def dist_path()
    File.dirname(__FILE__) + '/../../dist/target/torquebox-dist-bin.zip'
  end

  def dist_modules_path()
    File.dirname(__FILE__) + '/../../dist/target/torquebox-dist-modules.zip'
  end

  def json_metadata_path()
    File.dirname(__FILE__) + '/../../dist/target/build-metadata.json'
  end

  def gem_repo_path()
    File.dirname(__FILE__) + '/../../assembly/target/stage/gem-repo'
  end

  def standalone_xml_path()
    File.dirname(__FILE__) + '/../../assembly/target/stage/torquebox/jboss/standalone/configuration/standalone.xml'
  end
  
  def publish_documentation()
    if File.exist?(javadocs_path)
      dav_mkdir_p( build_base_url + '/javadocs' )
      dav_put_r( build_base_url + '/javadocs', javadocs_path )
    end
    dav_mkdir_p( build_base_url + '/yardocs' )
    dav_put_r( build_base_url + '/yardocs', yardocs_path )
    dav_put( build_base_url + '/torquebox-docs.epub', epub_path )
    dav_put( build_base_url + '/torquebox-docs.pdf', pdf_doc_path )
    dav_mkdir_p( build_base_url + '/html-docs' )
    dav_put_r( build_base_url + '/html-docs', html_docs_path )
  end

  def publish_distribution()
    dav_put( build_base_url + "/#{File.basename( json_metadata_path ) }", json_metadata_path )

    dav_put( build_base_url  + "/#{File.basename( dist_path ) }", dist_path )
    dav_put( build_base_url  + "/#{File.basename( dist_modules_path ) }", dist_modules_path )
    dav_put( build_base_url  + "/#{File.basename( standalone_xml_path ) }", standalone_xml_path )
  end

  def publish_gem_repo()
    dav_mkdir_p( build_gem_repo_url )
    dav_put_r( build_gem_repo_url, gem_repo_path )
  end

  def publish_artifact_list
    file = File.join( File.dirname( __FILE__ ), '..', 'target', 'published-artifacts.json' )
    File.open( file, 'w' ) { |f| f << @published_artifacts.to_json }
    dav_put( build_base_url + '/published-artifacts.json', file, false )
  end
end

Publisher.new( ARGV[0], ARGV[1] ).publish_all



