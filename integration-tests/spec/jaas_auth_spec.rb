require 'spec_helper' 
describe "jaas auth test" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RACK_ROOT: #{File.dirname(__FILE__)}/../apps/sinatra/auth
      RACK_ENV: development
    web:
      context: /jaas-auth
    
    ruby:
      version: #{RUBY_VERSION[0,3]}

    auth:
      default:
        domain: torquebox-auth
      configured:
        domain: hornetq
  END

  it "should fail authentication in the default domain with incorrect credentials" do
    visit "/jaas-auth/default/scott/penguin" 
    page.should have_content("failure")
  end

  it "should succeed authentication in the default domain with correct credentials" do 
    visit "/jaas-auth/default/admin/torquebox" 
    page.should have_content("success")
  end

  it "should fail authentication in a configured domain with incorrect credentials" do
    visit "/jaas-auth/configured/john/boy" 
    page.should have_content("failure")
  end

  it "should succeed authentication in a configured domain with correct credentials" do 
    visit "/jaas-auth/configured/john/needle" 
    page.should have_content("success")
  end

end

