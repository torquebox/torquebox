
module TorqueBox
  class Logger 

    def initialize name = nil
      category = name || (TORQUEBOX_APP_NAME if defined? TORQUEBOX_APP_NAME) || "TorqueBox"
      @logger = org.jboss.logging::Logger.getLogger( category.to_s.gsub('::','.') )
    end

    [:warn?, :error?, :fatal?].each do |method|
      define_method(method) { true }
    end

    attr_accessor :level

    def method_missing(method, *args, &block)
      # puts "JC: method_missing=#{method}"
      delegate = method
      if method.to_s.end_with?('?')
        delegate = "#{method.to_s.chop}_enabled?".to_sym
      end
      self.class.class_eval do
        define_method(method) do |*a, &b|
          params = [ a[0] || (b && b.call) ].compact
          @logger.send(delegate, *params)
        end
      end
      self.send(method, *args, &block)
    end

  end
end
