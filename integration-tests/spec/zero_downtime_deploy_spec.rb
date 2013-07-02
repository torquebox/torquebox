require 'spec_helper'

shared_examples_for 'zero downtime deploy' do |runtime_type|

  it 'should not reload without runtime restart' do
    visit '/reloader-rack?0'
    element = page.find_by_id('success')
    element.should_not be_nil

    seen_values = Set.new
    seen_values << element.text
    counter = 1
    while seen_values.size <= 3 && counter < 60 do
      visit "/reloader-rack?#{counter}"
      element = page.find_by_id('success')
      element.should_not be_nil
      seen_values << element.text
      counter += 1
    end

    seen_values.size.should == 1
  end

  it 'should reload with jmx runtime restart' do
    visit '/reloader-rack?0'
    element = page.find_by_id('success')
    element.should_not be_nil
    seen_values = Set.new
    seen_values << element.text
    counter = 1
    while seen_values.size <= 3 && counter < 60 do
      restart_runtime_with_jmx('web', "#{runtime_type}_runtime")
      visit "/reloader-rack?#{counter}"
      element = page.find_by_id('success')
      element.should_not be_nil
      seen_values << element.text
      counter += 1
    end

    seen_values.size.should > 3
  end

  it 'should reload with runtime restart marker' do
    visit '/reloader-rack?0'
    element = page.find_by_id('success')
    element.should_not be_nil
    seen_values = Set.new
    seen_values << element.text
    counter = 1
    while seen_values.size <= 3 && counter < 60 do
      restart_runtime_with_marker('web')
      visit "/reloader-rack?#{counter}"
      element = page.find_by_id('success')
      element.should_not be_nil
      seen_values << element.text
      counter += 1
      sleep 0.2
    end

    seen_values.size.should > 3
  end

  it 'should not drop requests while reloading' do
    seen_values = Set.new
    thread = Thread.new {
      300.times do |i|
        visit "/reloader-rack/?#{i}"
        element = page.find_by_id('success')
        element.should_not be_nil
        seen_values << element.text
      end
    }
    10.times do
      restart_runtime_with_jmx('web', "#{runtime_type}_runtime")
      sleep 0.1
    end
    thread.join
    # We'll probably see 10 values but it depends on thread scheduling
    seen_values.size.should > 3
  end

  def restart_runtime_with_jmx(pool, app)
    mbean("torquebox.pools:name=#{pool},app=#{app}") do |runtime|
      runtime.restart
    end
  end

  def restart_runtime_with_marker(pool)
    app_root = File.join(MUTABLE_APP_BASE_PATH, 'rack', 'reloader')
    FileUtils.mkdir_p(File.join(app_root, 'tmp'))
    marker = File.join(app_root, 'tmp', 'restart.txt')
    FileUtils.touch(marker)
  end

end

describe 'shared runtime' do

  mutable_app 'rack/reloader'
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RACK_ROOT: #{File.dirname(__FILE__)}/../target/apps/rack/reloader
      RACK_ENV: production
    web:
      context: /reloader-rack
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it_should_behave_like 'zero downtime deploy', 'shared'

end

describe 'bounded runtime' do

  mutable_app 'rack/reloader'
  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RACK_ROOT: #{File.dirname(__FILE__)}/../target/apps/rack/reloader
      RACK_ENV: production
    web:
      context: /reloader-rack
    pooling:
      web:
        min: 1
        max: 3
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it_should_behave_like 'zero downtime deploy', 'bounded'

end
