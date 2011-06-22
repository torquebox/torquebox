require 'options_macros'
require 'torquebox/configuration/global'

include TorqueBox::Configuration

describe "TorqueBox.configure using the GlobalConfiguration" do
  before(:each) do
    Thread.current[:torquebox_config] = TorqueBox::Configuration::GlobalConfiguration.new
    Thread.current[:torquebox_config_entry_map] = TorqueBox::Configuration::GlobalConfiguration::ENTRY_MAP
  end
  
  it "should parse the env" do
    config = TorqueBox.configure do
      env :foo
    end
    config['env'].should == 'foo'
  end

  it "should parse the env with a block arg" do
    config = TorqueBox.configure do |cfg|
      cfg.env :foo
    end

    config['env'].should == 'foo'
  end

  shared_examples_for "a hash" do
    before(:each) do
      validator = mock('Validator')
      validator.stub(:valid?).and_return(true)
      TorqueBox::Configuration::Validator.stub(:new).and_return(validator)
    end

    it "should parse as a hash" do
      config = TorqueBox.configure do |cfg|
        cfg.send( @method, {'foo' => 'bar'} )
      end

      config[@method].should == {'foo' => 'bar'}
    end

    it "should raise if given a block" do
      lambda {
        config = TorqueBox.configure do |cfg|
          cfg.send( @method, {'foo' => 'bar'}, &(lambda { }) )
        end
      }.should raise_error(TorqueBox::Configuration::ConfigurationError)
    end
  end

  shared_examples_for "a thing plus a hash" do
    before(:each) do
      validator = mock('Validator')
      validator.stub(:valid?).and_return(true)
      TorqueBox::Configuration::Validator.stub(:new).and_return(validator)
    end

    it "should parse a string and a hash" do
      config = TorqueBox.configure do |cfg|
        cfg.send( @method, 'a string', {'foo' => 'bar'} )
      end

      config[@method]['a string'].should == {'foo' => 'bar'}
    end

    it "should parse a symbol and a hash" do
      config = TorqueBox.configure do |cfg|
        cfg.send( @method, :a_sym, {'foo' => 'bar'} )
      end

      config[@method]['a_sym'].should == {'foo' => 'bar'}
    end

    it "should parse a constant and a hash" do
      config = TorqueBox.configure do |cfg|
        cfg.instance_eval "#{@method} AConstant, {'foo' => 'bar'}"
      end
      config[@method]['AConstant'].should == {'foo' => 'bar'}
    end

  end

  shared_examples_for "a thing plus a hash that does not allow a block" do
    it "should raise if given a block" do
      lambda {
        config = TorqueBox.configure do |cfg|
          cfg.send( @method, 'stuff', {'foo' => 'bar'}, &(lambda { }) )
        end
      }.should raise_error(TorqueBox::Configuration::ConfigurationError)
    end
  end

  describe '#authentication' do
    before(:each) { @method = 'authentication' }
    it_should_behave_like 'a thing plus a hash'
    it_should_behave_like "a thing plus a hash that does not allow a block"

    it_should_not_allow_invalid_options { authentication 'a-name', :foo => :bar }
    it_should_allow_valid_options { authentication 'a-name', :domain => :pizza }
  end

  describe '#environment' do
    before(:each) { @method = 'environment' }
    it_should_behave_like 'a hash'

    it "should be additive" do
      config = TorqueBox.configure do
        environment 'foo' => 'bar'
        environment 'bar' => 'baz'
      end

      config['environment'].should == { 'foo' => 'bar', 'bar' => 'baz' }
    end
  end

  describe '#ruby' do
    before(:each) { @method = 'ruby' }
    it_should_behave_like 'a hash'

    it_should_not_allow_invalid_options { ruby :foo => :bar }
    it_should_allow_valid_options { ruby :version => '1.9', :compile_mode => :jit }

    it_should_not_allow_invalid_option_values { ruby :version => '2' }
    it_should_not_allow_invalid_option_values { ruby :compile_mode => :bacon }
    it_should_allow_valid_option_values { ruby :version => '1.8', :compile_mode => :jit }
  end

  describe '#web' do
    before(:each) { @method = 'web' }
    it_should_behave_like 'a hash'

    it_should_not_allow_invalid_options { web :foo => :bar }
    it_should_allow_valid_options { web :context => '', :host => '', :rackup => '', :static => '' }
  end

  describe '#pool' do
    before(:each) { @method = 'pool' }
    it_should_behave_like 'a thing plus a hash'
    it_should_behave_like "a thing plus a hash that does not allow a block"

    it_should_not_allow_invalid_options { pool 'a-name', :foo => :bar }
    it_should_allow_valid_options { pool 'a-name', :type => :shared, :min => '', :max => '' }

    it_should_not_allow_invalid_option_values { pool 'a-name', :type => :bacon }
    it_should_allow_valid_option_values { pool 'a-name', :type => :shared }
  end

  %w{ queue topic }.each do |method|
    describe "##{method}" do
      before(:each) { @method = method }
      it_should_behave_like 'a thing plus a hash'

      it_should_not_allow_invalid_options {send(method, 'a-name', :foo => :bar) }
      it_should_allow_valid_options { send(method, 'a-name', :create => false, :durable => true, :remote_host => '') }
      
      it_should_not_allow_invalid_option_values { send(method, 'a-name', :create => :yep) }
      it_should_allow_valid_option_values { send(method, 'a-name', :create => true) }
    end
  end

  describe "#processor" do
    it "should only be valid inside a queue or topic" do
      lambda {
        TorqueBox.configure { processor 'Foo' }
      }.should raise_error(TorqueBox::Configuration::ConfigurationError)

      lambda {
        TorqueBox.configure do
          queue 'a-queue' do
            processor 'Foo'
          end
        end
      }.should_not raise_error(TorqueBox::Configuration::ConfigurationError)

      lambda {
        TorqueBox.configure do
          topic 'a-topic' do
            processor 'Foo'
          end
        end
      }.should_not raise_error(TorqueBox::Configuration::ConfigurationError)
    end

    it "should nest under its parent" do
      config = TorqueBox.configure do
        topic 'a-topic' do
          processor 'Foo'
        end
      end
      config['topic']['a-topic']['processor'].should == [['Foo', {}]]
    end

    
    it_should_not_allow_invalid_options do
      topic 'a-topic' do
        processor 'a-name', :foo => :bar
      end
    end
        
    it_should_allow_valid_options  do
      topic 'a-topic' do
        processor 'a-name', :concurrency => 1, :config => '', :filter => ''
      end
    end
    
  end

  describe "#options_for" do
    before(:each) { @method = 'options_for' }

    it_should_behave_like 'a thing plus a hash'
    it_should_behave_like "a thing plus a hash that does not allow a block"

    it_should_not_allow_invalid_options { options_for 'a-name', :foo => :bar }
    it_should_allow_valid_options { options_for 'a-name', :concurrency => 1, :disabled => true }

    it_should_not_allow_invalid_option_values { options_for 'a-name', :disabled => :bacon }
    it_should_allow_valid_option_values { options_for 'a-name', :disabled => false }
  end
  
  describe "#service" do
    before(:each) { @method = 'service'}

    it_should_behave_like 'a thing plus a hash'
    it_should_behave_like "a thing plus a hash that does not allow a block"

    it_should_not_allow_invalid_options { service 'a-name', :class => '', :foo => :bar }
    it_should_allow_valid_options { service 'a-name', :class => '', :config => '', :singleton => true }

    it_should_not_allow_invalid_option_values { service 'a-name', :class => '', :singleton => :bacon }
    it_should_allow_valid_option_values { service 'a-name', :class => '', :singleton => false }
  end

  describe "#to_metadata_hash" do
    before(:each) do
      @config = GlobalConfiguration.new.merge!({
                                                 'authentication' => {
                                                   'ham' => { :domain => :gravy }
                                                 },
                                                 'env' => :test,
                                                 'environment' => { :ham => 'biscuit' },
                                                 'job' => { 'a-job' => {
                                                     :cron => 'cronspec',
                                                     :class => FakeConstant.new( 'AJob' )
                                                   } },
                                                 'options_for' => { FakeConstant.new( 'Backgroundable' ) => {
                                                     :concurrency => 42 } },
                                                 'pool' => {
                                                   'web' => { :type => :shared }, 
                                                   'foo' => { :type => :bounded, :min => 1, :max => 2 } },
                                                 'queue' => {
                                                   'a-queue' => {
                                                     :create => false,
                                                     'processor' => [ [ FakeConstant.new( 'AProcessor' ),
                                                                        { :config => { :foo => :bar } } ] ]
                                                   },
                                                   'another-queue' => {},
                                                 },
                                                 'ruby' => { :version => '1.9' },
                                                 'service' => { 'a-service' => { :class => FakeConstant.new( 'AService' ) } },
                                                 'topic' => { 'a-topic' => { :durable => true } },
                                                 'web' => { :context => '/bacon' }
                                               })
      
      @metadata = @config.to_metadata_hash
    end

    it "should properly setup authentication" do
      @metadata['auth']['ham'].should == { 'domain' => :gravy }
    end
    
    it "should properly set the app env" do
      @metadata['application']['env'].should == :test
    end

    it "should properly set the environment" do
      @metadata['environment'].should == { 'ham' => 'biscuit' }
    end

    it "should properly set a job" do
      job = @metadata['jobs']['a-job']
      job.should_not be_nil
      job['job'].should == 'AJob'
      job['cron'].should == 'cronspec'
    end

    it "should properly set task options from options_for" do
      @metadata['tasks']['Backgroundable']['concurrency'].should == 42
    end

    it "should properly set pooling" do
      @metadata['pooling']['web'].should == 'shared'
      foo = @metadata['pooling']['foo']
      foo.should_not be_nil
      foo['min'].should == 1
      foo['max'].should == 2
    end

    it "should properly set a processor" do
      @metadata['messaging']['a-queue']['AProcessor'].should == { "config" => { 'foo' => :bar } }
    end

    it "should properly set a queue" do
      @metadata['queues']['another-queue'].should == { }
    end

    it "should not set a queue marked as create => false" do
      @metadata['queues']['a-queue'].should be_nil
    end

    it "should properly set ruby runtime options" do
      @metadata['ruby']['version'].should == '1.9'
    end

    it "should properly set a service" do
      @metadata['services']['AService'].should == { }
    end

    it "should properly set a topic" do
      @metadata['topics']['a-topic'].should == { 'durable' => true }
    end

    it "should properly set web" do
      @metadata['web']['context'].should == '/bacon'
    end
  end
end


