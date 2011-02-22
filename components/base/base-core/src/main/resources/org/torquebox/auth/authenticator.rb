module TorqueBox
  class Authenticator
    attr_reader :strategy
    attr_reader :domain

    def self.[](name)
      strategy, domain = load_config
      Authenticator.new(strategy, domain)
    end

    def initialize(strategy, domain)
      @strategy = strategy
      @domain   = domain
    end

    private
    def self.load_config
      ['file', 'other']
    end

  end
end
