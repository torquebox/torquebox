# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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
