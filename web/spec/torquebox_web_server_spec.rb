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

describe TorqueBox::Web do

  describe 'run' do
    it 'accepts create and mount options' do
      expect do
        TorqueBox::Web.run(:server => uuid, :auto_start => false,
                           :rack_app => lambda {})
      end.not_to raise_error
    end
  end

  describe 'server' do
    it 'warns on invalid options' do
      expect do
        TorqueBox::Web.server(uuid, :foo => 'bar')
      end.to raise_error(ArgumentError)
    end

    it 'has the same web component if passed the same name' do
      name = uuid
      web = TorqueBox::Web.server(name).web_component
      web2 = TorqueBox::Web.server(name).web_component
      expect(web).to eq(web2)
    end

    it 'has a new web component if passed a new name' do
      web = TorqueBox::Web.server(uuid).web_component
      web2 = TorqueBox::Web.server(uuid).web_component
      expect(web).not_to eq(web2)
    end
  end
end

describe TorqueBox::Web::Server do
  describe 'mount' do
    before(:each) do
      @name = uuid
      @server = TorqueBox::Web.server(@name, :auto_start => false)
    end

    after(:each) do
      @server.stop
    end

    it 'warns on invalid options' do
      expect do
        @server.mount(:foo => 'bar')
      end.to raise_error(ArgumentError)
    end
  end
end

describe TorqueBox::Web::Undertow do

  def value(v, field)
    f = v.java_class.declared_field(field)
    f.accessible = true
    f.value(v)
  end

  it 'should set correct builder fields' do
    opts = TorqueBox::Web::Undertow.builder(host: "hostname",
                                            port: 42,
                                            io_threads: 1,
                                            worker_threads: 2,
                                            buffer_size: 3,
                                            buffers_per_region: 4,
                                            direct_buffers?: false)
    opts.keys.sort.should == [:configuration, :host, :port]
    config = opts[:configuration]
    value(config, :io_threads).should == 1
    value(config, :worker_threads).should == 2
    value(config, :buffer_size).should == 3
    value(config, :buffers_per_region).should == 4
    value(config, :direct_buffers).should == false
    listener = value(config, :listeners).first
    value(listener, :host).should == "hostname"
    value(listener, :port).should == 42
  end
end
