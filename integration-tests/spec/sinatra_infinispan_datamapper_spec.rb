require 'spec_helper'


describe "sinatra with dm-infinispan-adapter" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/sinatra/datamapper
      env: development
    web:
      context: /sinatra-datamapper
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  after(:all) do
    FileUtils.rm_rf(File.join(File.dirname(__FILE__), '..', 'rubyobj.Muppet'))
  end

  it "should work" do
    visit "/sinatra-datamapper"
    page.should have_content('It Works!')
  end

  it "should list muppets" do
    visit "/sinatra-datamapper/muppets"
    page.should have_content('Muppet Count: 3')
    page.should have_content('Muppet 10: Big Bird')
    page.should have_content('Muppet 20: Snuffleupagus')
    page.should have_content('Muppet 30: Cookie Monster')
  end

  it "should find muppets by name" do
    visit '/sinatra-datamapper/muppet/name'
    page.should have_content('Snuffleupagus')
  end

  it "should find muppets by id" do
    visit '/sinatra-datamapper/muppet/id'
    page.should have_content('Snuffleupagus')
  end

  it "should find muppets by num" do
    visit '/sinatra-datamapper/muppet/num'
    page.should have_content('Snuffleupagus')
  end

  it "should find muppets by range" do
    visit '/sinatra-datamapper/muppet/range'
    page.should have_content('Snuffleupagus')
  end

  it "should find muppets by inclusive range" do
    visit '/sinatra-datamapper/muppet/inclusive-range'
    page.should have_content('Snuffleupagus')
  end

  it "should find muppets by like" do
    visit '/sinatra-datamapper/muppet/like'
    page.should have_content('Snuffleupagus')
  end

  it "should find muppets by date range" do
#    pending "A fix for date queries"
    visit '/sinatra-datamapper/muppet/date/range'
    page.should have_content('Snuffleupagus')
  end

  it "should delete muppets" do
    visit '/sinatra-datamapper/muppet/delete'
    page.should have_content('Hiding')
  end

end

