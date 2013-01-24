require 'options_macros'
require 'torquebox/configuration/global'

include TorqueBox::Configuration

describe "TorqueBox.configure using the GlobalConfiguration" do
  before(:each) do
    Thread.current[:torquebox_config] = TorqueBox::Configuration::GlobalConfiguration.new
    Thread.current[:torquebox_config_entry_map] = TorqueBox::Configuration::GlobalConfiguration::ENTRY_MAP
  end

  shared_examples_for "options" do
    before(:each) do
      validator = mock('Validator')
      validator.stub(:valid?).and_return(true)
      TorqueBox::Configuration::Validator.stub(:new).and_return(validator)
    end

    it "should parse a hash" do
      config = TorqueBox.configure do |cfg|
        cfg.send( @method, {'foo' => 'bar'} )
      end

      config['<root>'][@method].should == {'foo' => 'bar'}
    end

    it "should parse a block" do
      config = TorqueBox.configure do |cfg|
        cfg.send( @method ) { foo 'bar'}
      end
      config['<root>'][@method].should == { :foo => 'bar' }
    end
  end

  shared_examples_for "a thing with options" do
    before(:each) do
      validator = mock('Validator')
      validator.stub(:valid?).and_return(true)
      TorqueBox::Configuration::Validator.stub(:new).and_return(validator)
    end

    def assert_options(config, key, options)
      if @discrete
        config['<root>'][@method].should == [[key, options]]
      else
        config['<root>'][@method][key].should == options
      end

    end

    it "should parse a string and a hash" do
      config = TorqueBox.configure do |cfg|
        cfg.send( @method, 'a string', {'foo' => 'bar'} )
      end

      assert_options( config, 'a string', {'foo' => 'bar'} )
    end

    it "should parse a symbol and a hash" do
      config = TorqueBox.configure do |cfg|
        cfg.send( @method, :a_sym, {'foo' => 'bar'} )
      end

      assert_options( config, 'a_sym', {'foo' => 'bar'} )
    end

    it "should parse a constant and a hash" do
      config = TorqueBox.configure do |cfg|
        cfg.instance_eval "#{@method} AConstant, {'foo' => 'bar'}"
      end
      assert_options( config, 'AConstant', {'foo' => 'bar'} )
    end

    it "should parse a thing and a block" do
      config = TorqueBox.configure do |cfg|
        cfg.send( @method, 'a thing' ) { foo 'bar' }
      end

      assert_options( config, 'a thing', { :foo => 'bar' } )
    end

  end

  describe '#authentication' do
    before(:each) { @method = 'authentication' }
    it_should_behave_like 'a thing with options'

    it_should_not_allow_invalid_options { authentication 'a-name', :foo => :bar }
    it_should_allow_valid_options { authentication 'a-name', :domain => :pizza }
  end

  describe "#credential" do
    it "should only be valid inside authentication" do
      lambda {
        TorqueBox.configure { credential 'ham', 'biscuit' }
      }.should raise_error(TorqueBox::Configuration::ConfigurationError)

      lambda {
        TorqueBox.configure do
          authentication :default, :domain => 'ham' do
            credential 'ham', 'biscuit'
          end
        end
      }.should_not raise_error(TorqueBox::Configuration::ConfigurationError)
    end

    it "should nest under its parent" do
      config = TorqueBox.configure do
        authentication :default, :domain => 'ham' do
          credential 'ham', 'biscuit'
        end
      end
      config['<root>']['authentication']['default']['credential'].should == [['ham', 'biscuit']]
    end

    it "should nest under its parent properly when called more than once" do
      config = TorqueBox.configure do
        authentication :default, :domain => 'ham' do
          credential 'ham', 'biscuit'
          credential 'biscuit', 'gravy'
        end
      end
      config['<root>']['authentication']['default']['credential'].should == [['ham', 'biscuit'], ['biscuit', 'gravy']]
    end
  end

  describe '#environment' do
    before(:each) { @method = 'environment' }
    it_should_behave_like 'options'

    it "should be additive" do
      config = TorqueBox.configure do
        environment 'foo' => 'bar'
        environment 'bar' => 'baz'
      end

      config['<root>']['environment'].should == { 'foo' => 'bar', 'bar' => 'baz' }
    end
  end
  
  describe '#injection' do
    before(:each) { @method = 'injection' }
    it_should_behave_like 'options'
    
    it_should_not_allow_invalid_options { injection :foo => :bar }
    it_should_allow_valid_options { injection :enabled => true }
    it_should_allow_valid_options { injection :enabled => false }
    
  end

  describe '#ruby' do
    before(:each) { @method = 'ruby' }
    it_should_behave_like 'options'

    it_should_not_allow_invalid_options { ruby :foo => :bar }
    it_should_allow_valid_options { ruby :version => '1.9', :compile_mode => :jit, :debug => true, :interactive => true, :profile_api => true }

    it_should_not_allow_invalid_option_values { ruby :version => '2' }
    it_should_not_allow_invalid_option_values { ruby :compile_mode => :bacon }
    it_should_not_allow_invalid_option_values { ruby :debug => :sure }
    it_should_not_allow_invalid_option_values { ruby :interactive => :sure }
    it_should_not_allow_invalid_option_values { ruby :profile_api => :goforit }
    it_should_allow_valid_option_values { ruby :version => '1.8', :compile_mode => :jit }
  end

  describe '#web' do
    before(:each) { @method = 'web' }
    it_should_behave_like 'options'
    
    it_should_not_allow_invalid_options { web :foo => :bar }
    it_should_allow_valid_options { web :context => '', :host => '', :rackup => '', :static => '', :session_timeout => '1s' }
  end

  describe '#pool' do
    before(:each) { @method = 'pool' }
    it_should_behave_like 'a thing with options'

    # type is required
    it_should_not_allow_invalid_options { pool 'a-name', :min => 3, :max => 5 }

    it_should_not_allow_invalid_options { pool 'a-name', :foo => :bar }
    it_should_allow_valid_options { pool 'a-name', :type => :shared, :min => '', :max => '', :lazy => true }

    it_should_not_allow_invalid_option_values { pool 'a-name', :type => :bacon }
    it_should_not_allow_invalid_option_values { pool 'a-name', :type => :shared, :lazy => 'foo' }
    it_should_allow_valid_option_values { pool 'a-name', :type => :shared, :lazy => false }
  end

  %w{ queue topic }.each do |method|
    describe "##{method}" do
      before(:each) { @method = method }
      it_should_behave_like 'a thing with options'

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
      config['<root>']['topic']['a-topic']['processor'].should == [['Foo', {}]]
    end


    it_should_not_allow_invalid_options do
      topic 'a-topic' do
        processor 'AClass', :foo => :bar
      end
    end

    it_should_allow_valid_options  do
      topic 'a-topic' do
        processor 'AClass', :concurrency => 1, :config => '', :selector => '', :name => '', :durable => true, :client_id => 'client-id', :singleton=>true, :xa => true
      end
    end

  end

  describe "#options_for" do
    before(:each) { @method = 'options_for' }

    it_should_behave_like 'a thing with options'

    it_should_not_allow_invalid_options { options_for 'a-name', :foo => :bar }
    it_should_allow_valid_options { options_for 'a-name', :concurrency => 1, :disabled => true, :durable => true }

    it_should_not_allow_invalid_option_values { options_for 'a-name', :disabled => :bacon, :durable => :bacon }
    it_should_allow_valid_option_values { options_for 'a-name', :disabled => false, :durable => false }
  end

  describe "#service" do
    before(:each) do
      @method = 'service'
      @discrete = true
    end

    it_should_behave_like 'a thing with options'

    it_should_not_allow_invalid_options { service 'AClass', :foo => :bar }
    it_should_allow_valid_options { service 'AClass', :config => '', :singleton => true, :name => '' }

    it_should_not_allow_invalid_option_values { service 'AClass', :singleton => :bacon }
    it_should_allow_valid_option_values { service 'AClass', :singleton => false }

    it "should allow multiple services using the same class" do
      config = TorqueBox.configure do
        service 'AService'
        service 'AService'
      end

      config['<root>']['service'].should == [['AService', { }], ['AService', { }]]
    end

    it "should allow service configuration with a block" do
      config = TorqueBox.configure do
        service 'AConfiguredService' do
          config do
            food :biscuit
          end
        end
      end

      config['<root>']['service'].should == [["AConfiguredService", {"config"=>{:food=>:biscuit}}]]
    end
        
  end

  describe '#stomp' do
    before(:each) { @method = 'stomp' }
    it_should_behave_like 'options'

    it_should_not_allow_invalid_options { stomp :foo => :bar }
    it_should_allow_valid_options { stomp :host => 'example.com' }
  end


  describe "#stomplet" do
    before(:each) do
      @method = 'stomplet'
      @discrete = true
    end

    it_should_behave_like 'a thing with options'

    it_should_not_allow_invalid_options { stomplet 'AClass', :route => '/foo', :foo => :bar }
    it_should_allow_valid_options { stomplet 'AClass', :config => '',  :route => '/foo', :name => '' }

    it "should allow multiple stomplets using the same class" do
      config = TorqueBox.configure do
        stomplet 'AStomplet', :route => '/x'
        stomplet 'AStomplet', :route => '/y'
      end

      config['<root>']['stomplet'].should == [['AStomplet', {:route => '/x' }], ['AStomplet', { :route => '/y' }]]
    end
  end

  describe "#job" do
    before(:each) do
      @method = 'job'
      @discrete = true
    end

    it_should_behave_like 'a thing with options'

    it_should_not_allow_invalid_options { job 'AClass', :foo => :bar }
    it_should_allow_valid_options { job 'AClass', :config => '', :singleton => true, :name => '' , :cron => '234', :description => 'A description'}

    it_should_not_allow_invalid_option_values { job 'AClass', :singleton => :bacon }
    it_should_allow_valid_option_values { job 'AClass', :singleton => false, :cron => '234' }

    it "should allow multiple jobs using the same class" do
      config = TorqueBox.configure do
        job 'AJob', :cron => '1234'
        job 'AJob', :cron => '1234'
      end

      config['<root>']['job'].should == [['AJob', { :cron => '1234' }], ['AJob', { :cron => '1234' }]]
    end

    it "should allow jobs in modules" do
      config = TorqueBox.configure do
        job 'One::AJob', :cron => '1234'
        job 'Two::AJob', :cron => '1234'
      end
      
      config['<root>']['job'].should == [['One::AJob', { :cron => '1234' }],
                                         ['Two::AJob', { :cron => '1234' }]]
    end

    # https://issues.jboss.org/browse/TORQUE-986
    it "should allow job description" do
      config = TorqueBox.configure do
        job 'AJob', :cron => '1234', :description => 'A description'
      end

      config['<root>']['job'].should == [['AJob', { :cron => '1234', :description => 'A description' }]]
    end

    # https://issues.jboss.org/browse/TORQUE-999
    it "should allow job timeout" do
      config = TorqueBox.configure do
        job 'AJob', :cron => '1234', :timeout => '30s'
      end

      config['<root>']['job'].should == [['AJob', { :cron => '1234', :timeout => '30s' }]]
    end

    it "should allow jobs as constants" do
      config = TorqueBox.configure do |cfg|
        cfg.instance_eval("job JobX, :cron => '1234'")
        cfg.instance_eval("job Mod1::JobY, :cron => '1234'")
        cfg.instance_eval("job Mod2::Mod3::JobZ, :cron => '1234'")
      end

      config['<root>']['job'].should == [['JobX', { :cron => '1234' }],
                                         ['Mod1::JobY', { :cron => '1234' }],
                                         ['Mod2::Mod3::JobZ', { :cron => '1234' }]]
    end

    it "should allow cron to be set in a block" do
      lambda {
        TorqueBox.configure do
          job 'AJob' do
            cron '123'
          end
        end
      }.should_not raise_error(TorqueBox::Configuration::ConfigurationError)
    end

    it "should raise if cron doesn't get set in a block" do
      lambda {
        TorqueBox.configure do
          job 'AJob' do
          end
        end
      }.should raise_error(TorqueBox::Configuration::ConfigurationError)
    end

    it "should allow job configuration with a block" do
      config = TorqueBox.configure do
        job 'AConfiguredJob' do
          cron '123'
          config do
            food :biscuit
          end
        end
      end

      config['<root>']['job'].should == [["AConfiguredJob", {:cron=>"123", "config"=>{:food=>:biscuit}}]]
    end
        
  end

  describe "#to_metadata_hash" do
    before(:each) do
      @config = GlobalConfiguration.new
      @config['<root>'] = {
        'authentication' => {
          'ham' => {
            :domain => :gravy,
            'credential' => [['ham', 'biscuit'], ['biscuit', 'gravy']]
          }
        },
        'environment' => { :ham => 'biscuit' },
        'job' => [ [ FakeConstant.new( 'AJob' ), {
                       :cron => 'cronspec',
                       :name => 'a-job',
                       :config => { :foo => :bar }
                     } ],
                   [ FakeConstant.new( 'AnotherJob' ), {
                       :cron => 'cronspec',
                       :config => { :foo => :bar }
                     } ],
                   [ FakeConstant.new( 'AnotherJob' ), {
                       :cron => 'cronspec',
                       :config => { :foo => :bar }
                     } ] ],
        'options_for' => {
          FakeConstant.new( 'Backgroundable' ) => { :concurrency => 42, :durable => false },
          'messaging' => { :default_message_encoding => :biscuit },
          'jobs' => { :concurrency => 55 }
          
        },
        'pool' => {
          'web' => { :type => :shared },
          'foo' => { :type => :bounded, :min => 1, :max => 2 } },
        'queue' => {
          'a-queue' => {
            :create => false
          },
          'another-queue' => {},
        },
        'ruby' => { :version => '1.9' },
        'service' => [ [ FakeConstant.new( 'AService' ), {
                           :name => 'a-service',
                           :config => { :foo => :bar } } ],
                       [ FakeConstant.new( 'AnotherService' ), {
                           :config => { :foo => :bar } } ] ],
        'stomp' => { :host => 'hambiscuit.com' },
        'stomplet' => [ [ FakeConstant.new( 'AStomplet' ), {
                            :route => '/a',
                            :name => 'a-stomplet',
                            :config => { :foo => :bar } } ],
                        [ FakeConstant.new( 'AnotherStomplet' ), {
                            :route => '/b',
                            :config => { :foo => :bar } } ] ],
        'topic' => { 'a-topic' => {
            :durable => true,
            'processor' => [ [ FakeConstant.new( 'AProcessor' ),
                               {
                                 :name => 'a-proc',
                                 :durable => true,
                                 :client_id => 'client-id',
                                 :xa => false,
                                 :config => { :foo => :bar } } ] ] } },
        'web' => { :context => '/bacon',
                   :host => ['host1', 'host2'],
                   :session_timeout => '42m' }       
      }

      @metadata = @config.to_metadata_hash
    end

    it "should properly setup authentication" do
      @metadata['auth']['ham'].should == { 'domain' => :gravy, 'credentials' => { 'ham' => 'biscuit', 'biscuit' => 'gravy' } }
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

    it "should properly handle jobs for the same class without names" do
      @metadata['jobs']['AnotherJob'].should_not be_nil
      @metadata['jobs']['AnotherJob-1'].should_not be_nil
    end


    it "should properly set messaging options from options_for" do
      @metadata['messaging']['default_message_encoding'].should == 'biscuit'
    end

    it "should properly set jobs options from options_for" do
      @metadata['jobs']['concurrency'].should == 55
    end

    it "should properly set task options from options_for" do
      @metadata['tasks']['Backgroundable']['concurrency'].should == 42
      @metadata['tasks']['Backgroundable']['durable'].should == false
    end

    it "should properly set pooling" do
      @metadata['pooling']['web'].should == 'shared'
      foo = @metadata['pooling']['foo']
      foo.should_not be_nil
      foo['min'].should == 1
      foo['max'].should == 2
    end

    it "should properly set a processor" do
      @metadata['messaging']['a-topic']['AProcessor'].should == {
        "name" => 'a-proc',
        "durable" => true,
        "client_id" => 'client-id',
        "xa" => false,
        "config" => { 'foo' => :bar }
      }
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

    it "should properly set stomp options" do
      @metadata['stomp']['host'].should == 'hambiscuit.com'
    end

    it "should properly set a service" do
      @metadata['services']['a-service'].should == { 'service' => 'AService', 'config' => { 'foo' => :bar } }
    end

    it "should properly set a stomplet" do
      @metadata['stomp']['stomplets']['a-stomplet'].should == { 'class' => 'AStomplet', 'route' => '/a', 'config' => { 'foo' => :bar } }
    end

    it "should properly set a topic" do
      @metadata['topics']['a-topic'].should == { 'durable' => true }
    end

    it "should properly set context" do
      @metadata['web']['context'].should == '/bacon'
    end

    it "should properly set hosts" do
      @metadata['web']['host'].to_a.should == ['host1', 'host2']
    end

    it "should properly set session_timeout" do
      @metadata['web']['session_timeout'].should == '42m'
    end
  end
  
end


