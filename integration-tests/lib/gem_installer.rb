require 'rubygems'
require 'rubygems/dependency_installer'

class GemInstaller

  def self.with(versions,&block)
    installer = GemInstaller.new( versions )
    block.call(installer)
  end

  def initialize(versions={})
    @versions = versions
    @installer = Gem::DependencyInstaller.new
  end

  def install(gem_name, version=nil)
    if ( version.nil? )
      version = @versions[ gem_name.gsub(/-/, '_').to_sym ]
    end
    gem_dir = File.join( ENV['GEM_HOME'], 'gems', "#{gem_name}-#{version}*" )
    unless ( Dir[ gem_dir ].empty? )
      puts "Skipping #{gem_name}"
      return
    end
    puts "Must specify version of #{gem_name}" and return unless version
    puts "Installing #{gem_name} #{version}"
    @installer.install( gem_name, version )

  end

end
