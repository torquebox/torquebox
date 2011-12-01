require 'spec_helper'

# Run these remotely so our jruby_home, gem_home, etc are all setup
remote_describe "rake tasks" do

  it "should work" do
    output = rake('integ:sanity_check --trace')
    output.should include('sanity check passed')
  end

  def rake(cmd)
    `#{jruby_binary} -S rake -f #{File.dirname(__FILE__)}/../apps/rails3.1/basic/Rakefile #{cmd}`
  end

end

