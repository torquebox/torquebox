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

    retry_count = 0
    begin
      installer.execute
    rescue Gem::SystemExitException=>e2
    rescue Gem::Exception => e3
      retry_count += 1
      if retry_count > 8
        raise e3
      else
        puts "Error fetching remote gem - sleeping and retrying"
        sleep 1
        retry
      end
    end
    Gem.clear_paths
  end

  require gem_name
end

require 'rubygems'
force_require 'yard', '0.8.7.3'

FILES = "*/lib/**/*.rb"
OUTPUT_DIR = "target/yardocs/"

OPTIONS = [
           "doc",
           "--title", "TorqueBox Gems Documentation",
           "-o", OUTPUT_DIR,
           "--api", "public",
           "--no-api",
           "--legacy",
           "--no-save",
           "--exclude", "no-op/lib/*",
           "--exclude", ".+\/torquebox\/.+\/ext\/.+",
           FILES
          ]

puts "Generating yardocs on #{FILES} to #{OUTPUT_DIR}"

Dir.chdir( File.join( File.dirname( __FILE__ ), '..' ) ) do
  YARD::CLI::CommandParser.run( *OPTIONS )
end
