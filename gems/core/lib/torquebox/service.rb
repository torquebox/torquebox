# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

module TorqueBox
  # This class is a Ruby API to manipulating TorqueBox services (daemons).
  class Service
    class << self

      # List all services of this application.
      #
      # @return [Array<org.torquebox.services.RubyService>] the list
      #   of RubyService instances - see {TorqueBox::Service.lookup}
      #   for more details on these instances
      def list
        prefix = service_prefix.canonical_name
        suffix = '.create'
        service_names = TorqueBox::MSC.service_names.select do |service_name|
          name = service_name.canonical_name
          name.start_with?(prefix) && name.end_with?(suffix)
        end
        service_names.map do |service_name|
          TorqueBox::MSC.get_service(service_name).value
        end
      end

      # Lookup a service of this application by name.
      #
      # @param [String] name the service's name (as given in
      #   torquebox.rb or torquebox.yml)
      #
      # @return [org.torquebox.services.RubyService] The RubyService
      #   instance.
      #
      # @note The RubyService instances returned by this and the
      #   {TorqueBox::Service.list} methods are not instances of this
      #   class but are instead Java objects of type
      #   org.torquebox.services.RubyService. There are more methods
      #   available on these instances than what's shown in the
      #   example here, but only the methods shown are part of our
      #   documented API.
      #
      # @example Stop a running service
      #   service = TorqueBox::Service.lookup('my_service')
      #   service.name => 'my_service'
      #   service.started? => true
      #   service.status => 'STARTED'
      #   service.stop
      #   service.status => 'STOPPED'
      def lookup(name)
        service_name = service_prefix.append(name).append('create')
        service = TorqueBox::MSC.get_service(service_name)
        service.nil? ? nil : service.value
      end

      private

      def service_prefix
        TorqueBox::MSC.deployment_unit.service_name.append('service')
      end

    end
  end
end
