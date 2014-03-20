require 'securerandom'
require 'torquebox-core'

def jruby_command
  File.join(RbConfig::CONFIG['bindir'], RbConfig::CONFIG['ruby_install_name'])
end

def uuid
  SecureRandom.uuid
end
