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
  class MSC
    class << self

      def service_registry
        TorqueBox::Registry['service-registry']
      end

      def deployment_unit
        TorqueBox::Registry['deployment-unit']
      end

      def service_names
        service_registry.service_names
      end

      def get_service(service_name)
        service_registry.get_service(service_name)
      end

      # Returns (or yields over the items) of list of
      # services with (canonical) name matching provided
      # regular expression.
      #
      # @param [Regexp] Regular expression to match the name
      #
      # @param Optional block. If block is provided it'll
      #         iterate over the values. If there is no block
      #         given this method returns list of services
      def get_services(regexp, &block)
        services = []

        TorqueBox::MSC.service_names.each do |name|
          if name.canonical_name =~ regexp
            service = TorqueBox::MSC.get_service(name)
            services << service unless service.nil?
          end
        end

        if block
          services.each do |s|
            yield s
          end
        else
          services
        end
      end

      def java_web_context
        war_meta_data = deployment_unit.get_attachment(org.jboss.as.web.deployment.WarMetaData::ATTACHMENT_KEY)
        return nil if war_meta_data.nil? # no web component in this application
        jboss_web_meta_data = war_meta_data.getMergedJBossWebMetaData
        virtual_host = jboss_web_meta_data.virtual_hosts.first || 'default-host'
        context_path = jboss_web_meta_data.context_root
        context_path = "/#{context_path}" unless context_path.start_with?('/')
        deployment_service_name = org.jboss.msc.service.ServiceName.parse("jboss.web.deployment")
        service_name = deployment_service_name.append(virtual_host).append(context_path)
        get_service(service_name).value
      end

      # Wait for the given MSC service to start.
      #
      # @param [org.jboss.msc.service.Service] MSC service to wait on
      #
      # @return String the service state after waiting - one of DOWN,
      # STARTING, START_FAILED, UP, STOPPING, or REMOVED. This should
      # be UP unless something went wrong.
      def wait_for_service_to_start(service)
        unless service.state.to_s == 'UP'
          listener = org.torquebox.core.gem.MSCServiceListener.new(service)
          service.add_listener(listener)
          service.set_mode(org.jboss.msc.service.ServiceController::Mode::ACTIVE)
          listener.wait_for_start_or_failure(10, java.util.concurrent.TimeUnit::SECONDS)
        end
        service.state.to_s
      end
    end
  end
end
