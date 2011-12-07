require 'spec_helper'

# Run these remotely so our jruby_home, gem_home, etc are all setup
describe "rake tasks" do

  it "should work" do
    output = rake('integ:sanity_check --trace')
    output.should include('sanity check passed')
  end

  def rake(task)
    integ_jruby("-S rake -f #{File.dirname(__FILE__)}/../apps/rails3.1/basic/Rakefile #{task}")
  end

end

