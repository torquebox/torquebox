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

describe TorqueBox::Web::Server do

  before(:each) do
    @rack_app = lambda {}
  end

  describe 'find_or_create' do
    it 'warns on invalid options' do
      expect {
        TorqueBox::Web::Server.find_or_create(uuid, :foo => 'bar')
      }.to raise_error(ArgumentError)
    end

    it 'has the same web component if passed the same name' do
      name = uuid
      web = TorqueBox::Web::Server.find_or_create(name).web_component
      web2 = TorqueBox::Web::Server.find_or_create(name).web_component
      expect(web).to eq(web2)
    end

    it 'has a new web component if passed a new name' do
      web = TorqueBox::Web::Server.find_or_create(uuid).web_component
      web2 = TorqueBox::Web::Server.find_or_create(uuid).web_component
      expect(web).not_to eq(web2)
    end
  end

  describe 'run' do
    it 'accepts create and mount options' do
      expect {
        TorqueBox::Web::Server.run(uuid, :auto_start => false,
                                   :rack_app => @rack_app)
      }.not_to raise_error
    end
  end

  describe 'mount' do
    before(:each) do
      @name = uuid
      @server = TorqueBox::Web::Server.find_or_create(@name, :auto_start => false)
    end

    after(:each) do
      @server.stop
    end

    it 'warns on invalid options' do
      expect {
        @server.mount(:foo => 'bar')
      }.to raise_error(ArgumentError)
    end

    # it 'runs the init callback after registering' do
    #   ran_init = false
    #   init = lambda { ran_init = true }
    #   @server.mount(:rack_app => @rack_app, :init => init)
    #   expect(ran_init).to be_true
    # end
  end
end
