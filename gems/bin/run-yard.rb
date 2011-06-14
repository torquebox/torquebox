require 'rubygems'
require 'yard'

FILES = "*/lib/**/*.rb"
OUTPUT_DIR = "target/yardoc/"

OPTIONS = [
           "--title", "TorqueBox Gems Documentation",
           "-o", OUTPUT_DIR,
           FILES
          ]

puts "Generating yardocs on #{FILES} to #{OUTPUT_DIR}"

Dir.chdir( File.join( File.dirname( __FILE__ ), '..' ) ) do
  YARD::CLI::Yardoc.new.run( *OPTIONS )
end
