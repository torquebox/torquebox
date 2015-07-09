# A small wrapper for clearing GEM_HOME and GEM_PATH then
# executing the desired command
require 'rbconfig'

ENV.delete('GEM_HOME')
ENV.delete('GEM_PATH')

TESTING_ON_WINDOWS = ( java.lang::System.getProperty( "os.name" ) =~ /windows/i )

def jruby
  File.join(RbConfig::CONFIG["bindir"], RbConfig::CONFIG["ruby_install_name"])
end

command = ARGV.join(" ")
jruby_version = case RUBY_VERSION
                when /^1\.8\./ then ' --1.8'
                when /^1\.9\./ then ' --1.9'
                when /^2\.0\./ then ' --2.0'
                end
full_command = "#{jruby} #{jruby_version} #{command}"
full_command << " 2>&1" unless TESTING_ON_WINDOWS
puts `#{full_command}`
