require 'spec_helper'

# A remote group nested within a local one
describe "end-to-end twitter testing" do 

  # Deploy our apps
  deploy <<-END.gsub(/^ {4}/,'')
    application:
      root: #{File.dirname(__FILE__)}/../apps/rails3/twitter
    queues:
      tweets:
  END

  before(:all) do
    @default_dir = File.join(File.dirname(__FILE__), '..', 'Infinispan-FileCacheStore')
  end

  after(:all) do
    FileUtils.rm_rf @default_dir
  end

  # Runs locally using capybara DSL
  it "should retrieve the index using a Capybara DSL" do
    visit "/tweets"
    page.should have_content( "Last 20 tweets" )
    page.find("h1").text.should == "Tweets"
    if ( Capybara.current_driver == :browser )
      page.find("table")[:class].should == ''
    else
      page.find("table")[:class].should be_nil
    end
  end

  remote_describe "in-container tests" do
    require 'torquebox-core'
    include TorqueBox::Injectors

    # Runs remotely (in-container)
    it "should be running remotely" do
      inject('deployment-unit').should_not be_nil
      inject('service-registry').should_not be_nil
      TorqueBox::ServiceRegistry.lookup("jboss.messaging.default.jms.manager").should_not be_nil
      inject( Java::pl.goldmann.confitura.beans.TweetReader ).should_not be_nil
    end
  end

end
