require 'jruby/core_ext'

module Spec
  module Example
    module ExampleGroupMethods
      def deploy path
        @deployment = path
      end
      attr_reader :deployment
    end
  end
end

Spec::Runner.configure do |config|

  config.before(:all) do
    configuration = org.jboss.arquillian.impl.XmlConfigurationBuilder.new.build()
    @adaptor = org.jboss.arquillian.impl.DeployableTestBuilder.build(configuration)
    @adaptor.beforeSuite
    puts "JCr: self.class=#{self.class}"
    @adaptor.beforeClass(self.class.become_java!)
  end

  config.after(:all) do
    @adaptor.afterClass(self.class)
    @adaptor.afterSuite
  end

end
