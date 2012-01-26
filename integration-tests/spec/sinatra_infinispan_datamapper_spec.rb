require 'spec_helper'

TOUCHFILE = File.join( File.dirname(__FILE__).gsub( %r(\\:), ':' ).gsub( %r(\\\\), '\\' ), '..', 'target' )
FOO_FILE  = File.join( TOUCHFILE, 'dm-messaging-foofile.txt' )
BAR_FILE  = File.join( TOUCHFILE, 'dm-messaging-barfile.txt' )

describe "sinatra with dm-infinispan-adapter" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/sinatra/datamapper
      env: development
    web:
      context: /sinatra-datamapper
    ruby:
      version: #{RUBY_VERSION[0,3]}
    environment:
      FOO_FILE: #{FOO_FILE}
      BAR_FILE: #{BAR_FILE}

  END

  after(:all) do
    FileUtils.rm_rf(File.join(File.dirname(__FILE__), '..', 'rubyobj.Muppet'))
    FileUtils.rm_rf(File.join(File.dirname(__FILE__), '..', 'rubyobj.Coat'))
  end

  it "should work" do
    #pending
    visit "/sinatra-datamapper"
    page.should have_content('It Works!')
  end

  it "should list muppets" do
    #pending
    visit "/sinatra-datamapper/muppets"
    page.should have_content('Muppet Count: 3')
    page.should have_content('Muppet 10: Big Bird')
    page.should have_content('Muppet 20: Snuffleupagus')
    page.should have_content('Muppet 30: Cookie Monster')
  end

  it "should find muppets by name" do
    #pending
    visit '/sinatra-datamapper/muppet/name'
    page.should have_content('Snuffleupagus')
  end

  it "should find muppets by id" do
    #pending
    visit '/sinatra-datamapper/muppet/id'
    page.should have_content('Snuffleupagus')
  end

  it "should find muppets by num" do
    #pending
    visit '/sinatra-datamapper/muppet/num'
    page.should have_content('Snuffleupagus')
  end

  it "should find muppets by range" do
    #pending
    visit '/sinatra-datamapper/muppet/range'
    page.should have_content('Snuffleupagus')
  end

  it "should find muppets by inclusive range" do
    #pending
    visit '/sinatra-datamapper/muppet/inclusive-range'
    page.should have_content('Snuffleupagus')
  end

  it "should find muppets by like" do
    #pending
    visit '/sinatra-datamapper/muppet/like'
    page.should have_content('Snuffleupagus')
  end

  it "should find muppets by date range" do
    #pending
    visit '/sinatra-datamapper/muppet/date/range'
    page.should have_content('Snuffleupagus')
  end

  it "should delete muppets" do
    #pending
    visit '/sinatra-datamapper/muppet/delete'
    page.should have_content('Hiding')
  end

  it "should index records" do
    pending
    visit '/sinatra-datamapper'
    page.should have_content('indexed')
  end

  it "should work for always_backgrounded jobs on DataMapper::Resource" do
    touchfile = Pathname.new( FOO_FILE )
    FileUtils.rm_rf( touchfile )
    visit '/sinatra-datamapper/foo/hello'
    sleep 2
    touchfile.should exist
    File.read( touchfile ).strip.should eql( 'hello' )
    page.should have_content('success')
  end

  it "should work for ad hoc backgrounded jobs on DataMapper::Resource" do
    pending "Figuring out wtf is up with ad hoc backgrounding"
    touchfile = Pathname.new( BAR_FILE )
    FileUtils.rm_rf( touchfile )
    visit '/sinatra-datamapper/bar/world'
    sleep 2
    touchfile.should exist
    File.read( touchfile ).strip.should eql( 'world' )
    page.should have_content('success')
  end

end

