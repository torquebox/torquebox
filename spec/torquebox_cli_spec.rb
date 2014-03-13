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

describe TorqueBox::CLI do

  before(:each) do
    @spec_dir = File.dirname(__FILE__)
    @args = %W{-q}
  end

  it 'should override bind address' do
    @args += %W{-b 1.2.3.4}
    cli = TorqueBox::CLI.new(@args)
    cli.server.options[:host].should == '1.2.3.4'
  end

  it 'should override port' do
    @args += %W{-p 8765}
    cli = TorqueBox::CLI.new(@args)
    cli.server.options[:port].should == '8765'
  end

  it 'should override rackup file' do
    @args << File.join(@spec_dir, 'other_config.ru')
    cli = TorqueBox::CLI.new(@args)
    cli.server.options[:rackup].should end_with('other_config.ru')
  end

  it 'should override root directory' do
    @args += %W{--dir #{@spec_dir}}
    cli = TorqueBox::CLI.new(@args)
    cli.server.options[:root].should == @spec_dir
  end
end
