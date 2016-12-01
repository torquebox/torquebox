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

require 'securerandom'
require 'torquebox-core'
require 'torquebox/spec_helpers'

RSpec.configure do |config|
  config.raise_errors_for_deprecations!
  # TODO: Convert everything from old should format
  # to new expect format and delete the expect_with and
  # mock_with config below
  config.expect_with :rspec do |c|
    c.syntax = [:should, :expect]
  end
  config.mock_with :rspec do |c|
    c.syntax = [:should, :expect]
  end
end

def jruby_command
  File.join(RbConfig::CONFIG['bindir'], RbConfig::CONFIG['ruby_install_name'])
end

def uuid
  SecureRandom.uuid
end

def unzip(path)
  if windows?
    `jar.exe xf #{path}`
  else
    `jar xf #{path}`
  end
end

def windows?
  RbConfig::CONFIG['host_os'] =~ /mswin/
end

def macos?
  RbConfig::CONFIG['host_os'] =~ /darwin/
end
