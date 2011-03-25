
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
      m = method.to_s
      if m.end_with?('?') && !m.end_with?('enabled?')
        method = "#{m.chop}_enabled?".to_sym
      end
      @logger.send(method, *args, &block) 
    end

  end
end
