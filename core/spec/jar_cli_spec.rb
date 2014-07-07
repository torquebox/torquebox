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

end
