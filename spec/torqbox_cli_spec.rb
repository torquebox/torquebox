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

describe TorqBox::CLI do

  before(:each) do
    @spec_dir = File.dirname(__FILE__)
    @args = %W{-q}
  end

  it 'should override bind address' do
    pending 'figure out how to reliably find a non-localhost IP'
  end

  it 'should override port' do
    @args += %W{-p 8765}
    Dir.chdir(@spec_dir) do
      cli = TorqBox::CLI.new(@args)
      begin
        cli.start
        uri = URI.parse('http://localhost:8765/')
        response = Net::HTTP.get_response(uri)
        response.code.should == '200'
        response.body.should include('config.ru')
      ensure
        cli.stop
      end
    end
  end

  it 'should override rackup file' do
    @args << File.join(@spec_dir, 'other_config.ru')
    cli = TorqBox::CLI.new(@args)
    begin
      cli.start
      uri = URI.parse('http://localhost:8080/')
      response = Net::HTTP.get_response(uri)
      response.code.should == '200'
      response.body.should include('other_config.ru')
    ensure
      cli.stop
    end
  end

  it 'should override root directory' do
    @args += %W{--dir #{@spec_dir}}
    cli = TorqBox::CLI.new(@args)
    begin
      cli.start
      uri = URI.parse('http://localhost:8080/')
      response = Net::HTTP.get_response(uri)
      response.code.should == '200'
      response.body.should include('config.ru')
    ensure
      cli.stop
    end
  end
end
