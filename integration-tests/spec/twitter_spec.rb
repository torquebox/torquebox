require 'spec_helper'
require 'torquebox-messaging'

# A remote group nested within a local one
describe "end-to-end twitter testing" do 

  # Deploy our apps
  deploy <<-END.gsub(/^ {4}/,'')
    application:
      root: #{File.dirname(__FILE__)}/../apps/rails3/twitter
    web:
      context: /twitter
    ruby:
      version: #{RUBY_VERSION[0,3]}
    jobs:
      job.one:
        job: SimpleJob
        cron: '*/1 * * * * ?'
        config:
          color: blue
          an_array:
            - one
            - two
    queues:
      tweets:
      /queue/response:
        durable: false
      /queue/init_params:
        durable: false
  END

  before(:all) do
    @default_dir = File.join(File.dirname(__FILE__), '..', 'Infinispan-FileCacheStore')
  end

  after(:all) do
    FileUtils.rm_rf @default_dir
  end

  # Runs locally using capybara DSL
  it "should retrieve the index using a Capybara DSL" do
    visit "/twitter/tweets"
    page.should have_content( "Last 20 tweets" )
    page.find("h1").text.should == "Tweets"
    if ( Capybara.current_driver == :browser )
      page.find("table")[:class].should == ''
    else
      page.find("table")[:class].should be_nil
    end
  end

  context 'jobs' do
    it "should detect activity" do
      responseq = TorqueBox::Messaging::Queue.new( '/queue/response' )
      response = responseq.receive( :timeout => 120_000 )
      5.times do
        response.should == 'done'
        response = responseq.receive( :timeout => 120_000 )
      end
    end

    it "should have its init params" do
      responseq = TorqueBox::Messaging::Queue.new( '/queue/init_params' )
      response = responseq.receive( :timeout => 120_000 )

      response['color'].should == 'blue'
      response['an_array'].to_a.should == %w{ one two }
    end
  end

  remote_describe "in-container tests" do
    require 'torquebox-core'
    include TorqueBox::Injectors

    # Runs remotely (in-container)
    it "should be running remotely" do
      fetch('deployment-unit').should_not be_nil
      fetch('service-registry').should_not be_nil
      TorqueBox::ServiceRegistry.lookup("jboss.messaging.default.jms.manager").should_not be_nil
      fetch( Java::pl.goldmann.confitura.beans.TweetReader ).should_not be_nil
    end
  end

end
