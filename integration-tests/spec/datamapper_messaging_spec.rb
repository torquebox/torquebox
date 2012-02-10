require 'spec_helper'

TOUCHFILE = File.join( File.dirname(__FILE__).gsub( %r(\\:), ':' ).gsub( %r(\\\\), '\\' ), '..', 'target' )
FOO_FILE  = File.join( TOUCHFILE, 'dm-messaging-foofile.txt' )
BAR_FILE  = File.join( TOUCHFILE, 'dm-messaging-barfile.txt' )

describe "torquebox-messaging with datamapper" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/sinatra/datamapper-messaging
      env: development
    web:
      context: /datamapper-messaging
    ruby:
      version: #{RUBY_VERSION[0,3]}
    environment:
      FOO_FILE: #{FOO_FILE}
      BAR_FILE: #{BAR_FILE}

  END

  it "should support always_backgrounded jobs on DataMapper::Resource" do
    touchfile = Pathname.new( FOO_FILE )
    FileUtils.rm_rf( touchfile )
    visit '/datamapper-messaging/foo/hello'
    sleep 2
    touchfile.should exist
    File.read( touchfile ).strip.should eql( 'hello' )
    page.should have_content('success')
  end

  it "should support ad hoc backgrounded jobs on DataMapper::Resource" do
    touchfile = Pathname.new( BAR_FILE )
    FileUtils.rm_rf( touchfile )
    visit '/datamapper-messaging/bar/world'
    sleep 5
    touchfile.should exist
    File.read( touchfile ).strip.should eql( 'world' )
    page.should have_content('success')
  end

end


