# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
#
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
#
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

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
