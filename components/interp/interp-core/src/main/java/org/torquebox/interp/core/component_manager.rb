
module TorqueBox
  class ComponentManager
    class << self
      def lookup_component(name)
        @components ||= {}
        @components[name]
      end

      def register_component(name, component )
        @components ||= {}
        @components[name] = component
        Dispatcher.cleanup_application if defined?(Dispatcher)  # forces reload of Rails models
      end
    end
  end
end
