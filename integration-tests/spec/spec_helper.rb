require 'container'
require 'capybara/dsl'

Capybara.register_driver :selenium do |app|
  Capybara::Driver::Selenium.new(app, :browser => :firefox)
end

Capybara.default_driver = :selenium
Capybara.app_host = "http://localhost:8080"
Capybara.run_server = false

Spec::Runner.configure do |config|
  config.include Capybara
  config.after do
    Capybara.reset_sessions!
  end
end


