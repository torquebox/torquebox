require 'spec_helper'

describe "frozen gems" do

  GEM_NAMES = %w{ railties activesupport actionpack activerecord actionmailer activeresource }

  deploy "rails2/frozen-knob.yml"

  it "should include all required gems in its vendor directory" do
    visit "/frozen-rails"
    element = page.find("#success")
    element[:class].should == "frozen-rails"
    page.all(".load_path_element").inject(GEM_NAMES.dup) { |gems, path|
      gems.reject { |g| path.text =~ /frozen.*vendor\/rails\/#{g}\/lib/ }
    }.should be_empty
  end

end
