# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

require 'fileutils'
require 'tmpdir'
require 'rexml/document'
require 'rubygems'

require 'rubygems/installer'
require 'rubygems/dependency_installer'

class AssemblyTool

  attr_accessor :src_dir

  attr_accessor :base_dir
  attr_accessor :build_dir

  attr_accessor :torquebox_dir
  attr_accessor :gem_repo_dir

  attr_accessor :jboss_dir
  attr_accessor :jruby_dir

  attr_accessor :m2_repo
  
  attr_reader :options

  def modules
    @modules ||= []
  end

  def require_rubygems_indexer
    begin
      gem 'builder', '3.0.0'
    rescue Gem::LoadError=> e
      puts "Installing builder gem"
      require 'rubygems/commands/install_command'
      installer = Gem::Commands::InstallCommand.new
      installer.options[:args] = [ 'builder' ]
      installer.options[:version] = '3.0.0'
      installer.options[:document] = []
      begin
        installer.execute
      rescue Gem::SystemExitException=>e2
      end
    end
    require 'rubygems/indexer'
  end

  def initialize(options = {}) 
    @options = options

    @src_dir   = File.expand_path( File.dirname(__FILE__) + '/../../..' )
    @base_dir  = File.expand_path( File.dirname(__FILE__) + '/..' )
    @build_dir = @base_dir  + '/target/stage'

    @torquebox_dir = @build_dir  + '/torquebox'
    @gem_repo_dir  = @build_dir  + '/gem-repo'

    @jboss_dir = @torquebox_dir + '/jboss'
    @jruby_dir = @torquebox_dir + '/jruby'

    @m2_repo = options[:maven_repo_local ]

    if ( @m2_repo.nil? )
      if ( ENV['M2_REPO'] ) 
        @m2_repo = ENV['M2_REPO']
      else
        @m2_repo = ENV['HOME'] + '/.m2/repository'
      end
    end
  end

  def self.install_gem(gem)
     AssemblyTool.new().install_gem( gem, true )
  end

  def self.copy_gem_to_repo(gem)
    AssemblyTool.new().copy_gem_to_repo( gem, true )
  end

  def unzip(path)
    if windows?
      `jar.exe xf #{path}`
    elsif !options[:use_unzip]
      `jar xf #{path}`
    else
      `unzip -q #{path}`
    end
  end

  def windows?
    RbConfig::CONFIG['host_os'] =~ /mswin/
  end

  def install_gem(gem, update_index=false)
    puts "Installing #{gem}"
    install_dir = @jruby_dir + '/lib/ruby/gems/shared'
    opts = {
      :bin_dir     => @jruby_dir + '/bin',
      :env_shebang => true,
      :wrappers    => true,
      :document    => []
    }

    Gem.use_paths( install_dir )
    installer = Gem::DependencyInstaller.new( opts )
    retry_count = 0
    begin
      installer.install( gem )
    rescue Gem::Exception  => e
      retry_count += 1
      if retry_count > 8
        raise e
      else
        puts "Error fetching remote gem - sleeping and retrying"
        sleep 1
        retry
      end
    end
    generate_windows_bat_files( gem, opts, install_dir )
    copy_gem_to_repo(gem, update_index) if File.exist?( gem )
  end

  def generate_windows_bat_files(gem, opts, install_dir)
    # Completely hacked together from JRuby .bat templates and RubyGems
    bin_dir = opts[:bin_dir] || Gem.bindir( install_dir )
    installer = Gem::DependencyInstaller.new( opts )
    tuple = installer.find_spec_by_name_and_version( gem ).first
    spec = tuple.spec
    source = tuple.source
    local_spec = source.fetch_spec(spec.name_tuple)
    local_spec.executables.each do |filename|
      script_name = filename + ".bat"
      script_path = File.join( bin_dir, File.basename( script_name ) )
      File.open( script_path, 'wb', 0755 ) do |file|
        file.puts <<-TEXT.gsub( /^ {10}/,'' )
          @ECHO OFF
          IF NOT "%~f0" == "~f0" GOTO :WinNT
          @"jruby" -S "#{filename}" %1 %2 %3 %4 %5 %6 %7 %8 %9
          GOTO :EOF
          :WinNT
          @"%~dp0jruby.exe" "%~dpn0" %*
          TEXT
      end
    end
  end

  def copy_gem_to_repo(gem, update_index=false)
    FileUtils.mkdir_p gem_repo_dir + '/gems'
    FileUtils.cp gem, gem_repo_dir + '/gems'
    update_gem_repo_index if update_index
  end

  def update_gem_repo_index
    puts "Updating index" 
    require_rubygems_indexer()
    opts = {
    }
    indexer = Gem::Indexer.new( gem_repo_dir )
    indexer.generate_index
  end

  def self.install_module(name, path)
     AssemblyTool.new().install_module( name, path )
  end

  def install_module(name, path, dest_suffix = nil, remember = true)
    puts "Installing #{name} from #{path}"
    dest_suffix ||= "/modules/system/layers/torquebox/org/torquebox/#{name}/main"
    FileUtils.mkdir_p( @jboss_dir )
    Dir.chdir( @jboss_dir ) do
      dest_dir = Dir.pwd + dest_suffix
      FileUtils.rm_rf dest_dir
      FileUtils.mkdir_p File.dirname( dest_dir )
      FileUtils.cp_r path, dest_dir
    end
    modules << name if remember
  end

  def install_polyglot_module(name, version)
    artifact_path = "#{m2_repo}/org/projectodd/polyglot-#{name}/#{version}/polyglot-#{name}-#{version}-module.zip"
    Dir.mktmpdir do |artifact_dir|
      Dir.chdir( artifact_dir ) do
        unzip( artifact_path )
      end
      install_module( name, artifact_dir, "/modules/system/layers/polyglot/org/projectodd/polyglot/#{name}/main", false )
    end
  end

end

