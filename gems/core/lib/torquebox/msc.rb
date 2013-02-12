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

require 'torquebox/injectors'

module TorqueBox
  class MSC
    class << self
      include TorqueBox::Injectors

      def service_registry
        fetch('service-registry')
      end

      def deployment_unit
        fetch('deployment-unit')
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
    end
  end
end
