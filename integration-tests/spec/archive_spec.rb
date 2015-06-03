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

require 'spec_helper'

# None of these specs change based on our mode, so only run them once
if embedded_from_disk?
  feature "archived applications" do

    before(:each) do
      @tmpdir = Dir.mktmpdir("tmptorqueboxjar", ".")
      @old_gemfile = ENV.delete('BUNDLE_GEMFILE')
    end

    after(:each) do
      FileUtils.rm_rf(@tmpdir)
      ENV['BUNDLE_GEMFILE'] = @old_gemfile if @old_gemfile
    end

    it "can execute scripts inside a basic jar" do
      with_archive("#{apps_dir}/rack/basic", :jar) do |archive|
        output = `java -jar #{archive} -S torquebox --version`.split('\n')
        output.first.should include(TorqueBox::VERSION)
      end
    end

    it "can execute scripts inside a basic war" do
      with_archive("#{apps_dir}/rack/basic", :war) do |archive|
        output = `java -jar #{archive} -S torquebox --version`.split('\n')
        output.first.should include(TorqueBox::VERSION)
      end
    end

    it "can execute scripts inside a basic jar with main specified" do
      app_dir = "#{apps_dir}/rack/basic"
      with_archive(app_dir, :jar, %W(--main main.rb)) do |archive|
        output = `java -jar #{archive} -S torquebox --version`.split('\n')
        output.first.should include(TorqueBox::VERSION)
      end
    end

    it "can execute rake tasks from rails apps" do
      with_archive("#{apps_dir}/rails4/basic", :war) do |archive|
        output = `java -jar #{archive} -S rake about 2>&1`.split('\n')
        output.first.should include('About your application')
      end
    end

    def with_archive(app_dir, type, opts = [])
      Dir.chdir(app_dir) do
        TorqueBox::CLI.new(%W(#{type} -q --name test.#{type}) + opts).run
        File.exist?("test.#{type}").should == true
      end
      FileUtils.mv("#{app_dir}/test.#{type}", "#{@tmpdir}/test.#{type}")
      Dir.chdir(@tmpdir) do
        yield "test.#{type}"
      end
    end
  end
end
