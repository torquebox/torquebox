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

describe TorqueBox::Web::CLI do

  before(:each) do
    ENV['TORQUEBOX_CLI_SPECS'] = 'true'
    @spec_dir = File.dirname(__FILE__)
    @args = %W{run --dir #{apps_dir}/rack/basic -q}
  end

  after(:each) do
    ENV['TORQUEBOX_CLI_SPECS'] = nil
  end

  it 'should override bind address' do
    @args += %W{-b 1.2.3.4}
    TorqueBox::CLI.new(@args)
    options = TorqueBox::CLI.extensions['run'].options
    options[:host].should == '1.2.3.4'
  end

  it 'should override port' do
    @args += %W{-p 8765}
    TorqueBox::CLI.new(@args)
    options = TorqueBox::CLI.extensions['run'].options
    options[:port].should == '8765'
  end

  it 'should override rackup file' do
    @args << 'other_config.ru'
    TorqueBox::CLI.new(@args)
    options = TorqueBox::CLI.extensions['run'].options
    options[:rackup].should end_with('other_config.ru')
  end

  it 'should override root directory' do
    dir = "#{apps_dir}/rack/other"
    @args += %W{--dir #{dir}}
    TorqueBox::CLI.new(@args)
    options = TorqueBox::CLI.extensions['run'].options
    options[:root].should == dir
  end
end
