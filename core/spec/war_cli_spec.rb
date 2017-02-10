# Copyright 2014 Red Hat, Inc, and individual contributors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

require "spec_helper"
require "fileutils"

describe TorqueBox::CLI::War do

  before(:each) do
    @tmpdir = Dir.mktmpdir("tmptorqueboxwar", ".")
  end

  after(:each) do
    FileUtils.rm_rf(@tmpdir)
  end

  it "sets RACK_ENV and RAILS_ENV to --env value" do
    Dir.chdir(@tmpdir) do
      TorqueBox::CLI.new(%W(war -q --no-include-jruby --no-bundle-gems
                            --no-precompile-assets --name test.war
                            --env foobarbaz)).run
      File.exist?("test.war").should == true
      unzip("test.war")
      jar = Dir.glob("WEB-INF/lib/app.jar").first
      jar.should_not be_nil
      unzip(jar)
      File.exist?("META-INF/app.properties").should == true
      app_properties = File.read("META-INF/app.properties")
      app_properties.should include("foobarbaz")
    end
  end

  it "doesn't continually grow war size" do
    Dir.chdir(@tmpdir) do
      war = "test.war"
      cli_cmd = %W(war -q --no-bundle-gems --name #{war})
      TorqueBox::CLI.new(cli_cmd).run
      File.exist?(war).should == true
      original_size = File.size(war)
      2.times do
        TorqueBox::CLI.new(cli_cmd).run
        new_size = File.size(war)
        # We won't end up with identical war sizes every time but they
        # should be very close in size.
        diff = (new_size - original_size).abs
        percent_diff = diff * 100.0 / original_size
        percent_diff.should be < 1
      end
    end
  end

end
