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

  it "should work" do
    pending "a solution" do
    visit "/sinatra-datamapper"
    page.should have_content('It Works!')
    end
  end

  it "should list muppets" do
    pending "a solution" do
    visit "/sinatra-datamapper/muppets"
    page.should have_content('Muppet Count: 3')
    page.should have_content('Muppet 10: Big Bird')
    page.should have_content('Muppet 20: Snuffleupagus')
    page.should have_content('Muppet 30: Cookie Monster')
    end
  end

  it "should find muppets by name" do
    pending "a solution" do
    visit '/sinatra-datamapper/muppet/name'
    page.should have_content('Snuffleupagus')
    end
  end

  it "should find muppets by id" do
    pending "a solution" do
    visit '/sinatra-datamapper/muppet/id'
    page.should have_content('Snuffleupagus')
    end
  end

  it "should find muppets by num" do
    pending "a solution" do
    visit '/sinatra-datamapper/muppet/num'
    page.should have_content('Snuffleupagus')
    end
  end

  it "should find muppets by range" do
    pending "a solution" do
    visit '/sinatra-datamapper/muppet/range'
    page.should have_content('Snuffleupagus')
    end
  end

  it "should find muppets by inclusive range" do
    pending "a solution" do
    visit '/sinatra-datamapper/muppet/inclusive-range'
    page.should have_content('Snuffleupagus')
    end
  end

  it "should find muppets by like" do
    pending "a solution" do
    visit '/sinatra-datamapper/muppet/like'
    page.should have_content('Snuffleupagus')
    end
  end

  it "should delete muppets" do
    pending "a solution" do
    visit '/sinatra-datamapper/muppet/delete'
    page.should have_content('Hiding')
    end
  end

end

