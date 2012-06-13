require 'fileutils'
require 'spec_helper'
require 'torquebox-messaging'

describe "torquebox-messaging with datamapper" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/sinatra/datamapper-messaging
      env: development
    web:
      context: /datamapper-messaging
    ruby:
      version: #{RUBY_VERSION[0,3]}
    queues:
      /queue/backchannel:
  END

  before(:each) do
    @queue = TorqueBox::Messaging::Queue.new( '/queue/backchannel' )
  end

  after(:all) do
    db = File.join(File.dirname(__FILE__), '..', 'apps', 'sinatra',
                   'datamapper-messaging', 'dm-messaging-test.db')
    FileUtils.rm_f(db)
  end
  
  it "should support always_backgrounded jobs on DataMapper::Resource" do
    pending 'JRuby 1.7-compatible do_sqlite3 release' if JRUBY_VERSION =~ /^1\.7/
    visit '/datamapper-messaging/foo/hello'
    page.should have_content('success')
    @queue.receive( :timeout => 120_000 ).should == 'hello'
  end

  it "should support ad hoc backgrounded jobs on DataMapper::Resource" do
    pending 'JRuby 1.7-compatible do_sqlite3 release' if JRUBY_VERSION =~ /^1\.7/
    visit '/datamapper-messaging/bar/world'
    page.should have_content('success')
    @queue.receive( :timeout => 120_000 ).should == 'world'
  end

end


