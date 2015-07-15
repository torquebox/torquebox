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

describe TorqueBox::CLI::Jar do

  before(:each) do
    @jar = TorqueBox::CLI::Jar.new
    @tmpdir = Dir.mktmpdir("tmptorqueboxjar", ".")
  end

  after(:each) do
    FileUtils.rm_rf(@tmpdir)
  end

  it "doesn't modify Gemfile.lock" do
    lockfile = "#{@tmpdir}/orig_Gemfile.lock"
    File.open(lockfile, "w") do |file|
      file.write("foobarbaz")
    end
    @jar.copy_and_restore_lockfile(@tmpdir, lockfile, "abcd")
    expect(File.read(lockfile)).to eq("abcd")
  end

  it "doesn't create Gemfile.lock if it didn't exist before" do
    lockfile = "#{@tmpdir}/orig_Gemfile.lock"
    File.open(lockfile, "w") do |file|
      file.write("foobarbaz")
    end
    @jar.copy_and_restore_lockfile(@tmpdir, lockfile, nil)
    expect(File.exist?(lockfile)).to be false
  end

  it "doesn't include excluded paths" do
    Dir.chdir(@tmpdir) do
      File.new("excludeme", "w")
      Dir.mkdir("includeme")
      File.new("includeme/excludeme", "w")
      TorqueBox::CLI.new(%W(jar --exclude /excludeme -q --no-include-jruby
                            --no-bundle-gems --name test.jar)).run
      File.exist?("test.jar").should == true
      unzip("test.jar")
      File.exist?("app/excludeme").should == false
      File.exist?("app/includeme/excludeme").should == true
    end
  end

  it "includes .sprockets-manifest.json file" do
    Dir.chdir(@tmpdir) do
      File.new(".sprockets-manifest.json", "w")
      File.new(".sprockets-manifest-foobarbaz.json", "w")
      File.new("baz", "w")
      TorqueBox::CLI.new(%W(jar -q --no-include-jruby
                            --no-bundle-gems --name test.jar)).run
      File.exist?("test.jar").should == true
      unzip("test.jar")
      File.exist?("app/.sprockets-manifest.json").should == true
      File.exist?("app/.sprockets-manifest-foobarbaz.json").should == true
      File.exist?("app/baz").should == true
    end
  end

  it "doesn't include most dotfiles" do
    Dir.chdir(@tmpdir) do
      Dir.mkdir(".git")
      File.new(".git/foo", "w")
      File.new(".bar", "w")
      File.new("baz", "w")
      TorqueBox::CLI.new(%W(jar -q --no-include-jruby
                            --no-bundle-gems --name test.jar)).run
      File.exist?("test.jar").should == true
      unzip("test.jar")
      File.exist?("app/.git/foo").should == false
      File.exist?("app/.bar").should == false
      File.exist?("app/baz").should == true
    end
  end

end
