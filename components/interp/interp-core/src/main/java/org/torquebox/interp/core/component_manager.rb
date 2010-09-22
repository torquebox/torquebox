
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
      end
    end
  end
end
