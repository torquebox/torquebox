
module TorqueBox
  class Logger 

    def initialize name = nil
      @logger = org.jboss.logging::Logger.getLogger( name || (TORQUEBOX_APP_NAME if defined? TORQUEBOX_APP_NAME) || "TorqueBox" )
    end

    [:warn?, :error?, :fatal?].each do |method|
      define_method(method) { true }
    end

    attr_accessor :level

    def method_missing(method, *args, &block)
      # puts "JC: method_missing=#{method}"
      if block_given?
        self.class.class_eval do
          define_method(method) do |*a, &b|
            @logger.send(method, a[0] || b.call)
          end
        end
        self.send(method, yield)
      else
        delegate = method
        if method.to_s.end_with?('?')
          delegate = "#{method.to_s.chop}_enabled?".to_sym
        end
        self.class.class_eval do
          define_method(method) do |*a, &b|
            @logger.send(delegate, *a, &b) 
          end
        end
        self.send(method, *args, &block)
      end
    end

  end
end
