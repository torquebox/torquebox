def force_require(gem_name, version)
  begin
    gem gem_name, version
  rescue Gem::LoadError=> e
    puts "Installing #{gem_name} gem v#{version}"
    require 'rubygems/commands/install_command'
    installer = Gem::Commands::InstallCommand.new
    installer.options[:args] = [ gem_name ]
    installer.options[:version] = version
    installer.options[:generate_rdoc] = false
    installer.options[:generate_ri] = false
    begin
      installer.execute
    rescue Gem::SystemExitException=>e2
    end
    Gem.clear_paths
  end

  require gem_name
end

require 'rubygems'
force_require 'yard', '0.7.2'
force_require 'maruku', '0.6.0'

FILES = "*/lib/**/*.rb"
OUTPUT_DIR = "target/yardocs/"

OPTIONS = [
           "--title", "TorqueBox Gems Documentation",
           "-o", OUTPUT_DIR,
           FILES
          ]

puts "Generating yardocs on #{FILES} to #{OUTPUT_DIR}"

Dir.chdir( File.join( File.dirname( __FILE__ ), '..' ) ) do
  YARD::CLI::Yardoc.new.run( *OPTIONS )
end
