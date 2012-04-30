require 'spec_helper'
require 'fileutils'

BASEDIR    = File.join( File.dirname(__FILE__).gsub( %r(\\:), ':' ).gsub( %r(\\\\), '\\' ), '..', 'target' )
TOUCHFILE  = File.join( BASEDIR, 'alacarte-runtime-touchfile.txt' )

describe "alacarte runtime initialization test" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/runtime_initialization
      env: development
    
    environment:
      TOUCHFILE: #{TOUCHFILE}
    
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  remote_describe "torquebox_init" do
    include TorqueBox::Injectors
    it "should work" do
      queue = inject('/queues/tb_init_test')
      queue.receive.should == "M1M2"
    end
  end

  remote_describe "runtime context in a service" do
    include TorqueBox::Injectors
    it "should set ENV['TORQUEBOX_CONTEXT'] to 'services'" do
      queue = inject('/queues/service_context')
      queue.receive.should == "services"
    end
  end

  remote_describe "runtime context in a job" do
    include TorqueBox::Injectors
    it "should set ENV['TORQUEBOX_CONTEXT'] to 'jobs'" do
      queue = inject('/queues/jobs_context')
      queue.receive.should == "jobs"
    end
  end

end

