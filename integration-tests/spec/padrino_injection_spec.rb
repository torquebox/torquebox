require 'spec_helper'

unless TorqueSpec.domain_mode   # singleton services won't even deploy [TORQUE-776]

describe 'padrino injection test' do

    deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RAILS_ROOT: #{File.dirname(__FILE__)}/../apps/padrino/injection
      RAILS_ENV: development
    web:
      context: /padrino-injection
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it 'should inject from app.rb' do
    visit '/padrino-injection/from-app'
    page.should have_content('service is AppService')
    page.should have_content('queue is /queue/app')
  end

  it 'should inject from models' do
    visit '/padrino-injection/from-models'
    page.should have_content('service is FooService')
    page.should have_content('queue is /queue/foo')
  end

  it 'should inject from lib' do
    visit '/padrino-injection/from-lib'
    page.should have_content('service is BarService')
    page.should have_content('queue is /queue/bar')
  end

  it 'should inject from controller' do
    visit '/padrino-injection/from-controller'
    page.should have_content('service is ControllerService')
    page.should have_content('queue is /queue/controller')
  end
end

end
