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

require 'torquebox/msc'
require 'torquebox/registry'
require 'torquebox/service_registry'

module TorqueBox

  class InjectionError < StandardError
  end

  def self.fetch(something)
    unless TorqueBox::Registry.has_key?(something.to_s)
      handler_registry = TorqueBox::ServiceRegistry['torquebox.core.injection.injectable-handler-registry']
      # handler_registry should only be nil when running outside of
      # TorqueBox so we just return the nil value and skip everything
      # else to facilitate testing outside the container
      return nil if handler_registry.nil?
      handler = handler_registry.get_handler(something)
      raise InjectionError.new("Invalid injection - #{something}") if handler.nil?
      injectable = handler.handle(something, true)
      service_name = injectable.get_service_name(TorqueBox::Registry['service-target'],
                                                 TorqueBox::Registry['deployment-unit'])
      service = TorqueBox::ServiceRegistry.registry.getService(service_name)
      raise InjectionError.new("Service not found for injection - #{something}") if service.nil?
      state = TorqueBox::MSC.wait_for_service_to_start(service, 45)
      raise InjectionError.new("Injected service failed to start - #{something}") if state != 'UP'
      value = service.value
      raise InjectionError.new("Injected service had no value - #{something}") if value.nil?
      value = value.convert(JRuby.runtime) if value.respond_to?(:convert)
      TorqueBox::Registry.merge!(something.to_s => value)
    end
    TorqueBox::Registry[something.to_s]
  end

  module Injectors

    def fetch(something)
      TorqueBox.fetch(something)
    end
    alias_method :__inject__, :fetch

    %w{ msc service cdi jndi queue topic }.each do |type|
      define_method("fetch_#{type}".to_sym) do |key|
        fetch(key)
      end
    end

  end
end

# Temporarily workaround TorqueSpec needing an inject(...) method
module TorqueSpec
  class Daemon
    def inject(something)
      TorqueBox.fetch(something)
    end
  end
end
