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

    it 'runs the init callback after registering' do
      ran_init = false
      init = lambda { ran_init = true }
      @server.mount(:rack_app => @rack_app, :init => init)
      expect(ran_init).to be_true
    end
  end
end
