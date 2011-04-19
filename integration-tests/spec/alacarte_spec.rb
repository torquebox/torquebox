require 'spec_helper'
require 'set'

shared_examples_for "alacarte" do

  before(:each) do
    @file = File.join( File.dirname( __FILE__ ), '..', 'target', 'touchfile.txt' )
    File.delete(@file) rescue nil
  end

  it "should detect file writing activity" do
    seen_values = Set.new
    10.times do 
      sleep 1
      if File.exist?(@file)
        File.open(@file) do |f|
          seen_values << f.read
        end
      end
    end
    seen_values.size.should be > 5
  end

end

describe "jobs alacarte" do
  deploy "alacarte/jobs-knob.yml"
  it_should_behave_like "alacarte"
end
describe "services alacarte" do
  deploy "alacarte/services-knob.yml"
  it_should_behave_like "alacarte"
end
