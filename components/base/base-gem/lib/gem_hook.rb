
require 'torquebox/kernel'
require 'torquebox/component_manager'
require 'torquebox/injectors'
require 'torquebox/logger'

begin
  require 'active_support/cache/torque_box_store'
rescue Exception
  # pre-loadable only if activesupport is in $:
end
