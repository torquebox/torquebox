# A small wrapper for clearing GEM_HOME and GEM_PATH then
# executing the desired command
require 'rbconfig'

ENV.delete('GEM_HOME')
ENV.delete('GEM_PATH')

def jruby
  File.join(RbConfig::CONFIG["bindir"], RbConfig::CONFIG["ruby_install_name"])
end

command = ARGV.join(" ")
jruby_version = RUBY_VERSION =~ /^1\.9\./ ? " --1.9" : " --1.8"
puts `#{jruby} #{jruby_version} #{command}`
