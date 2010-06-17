
require 'optparse'

require 'torquebox/container/foundation_command'
require 'torquebox/naming/naming_service'

module TorqueBox
  module Naming
    module Commands
      class NamingService < TorqueBox::Container::FoundationCommand

        def initialize()
          super
        end

        def configure(container)
          container.enable( TorqueBox::Naming::NamingService )
        end

        def parser_options(opts)
          super(opts)
        end

      end
    end
  end
end
