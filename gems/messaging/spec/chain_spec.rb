require 'torquebox/messaging/processor_middleware/chain'

class MockMWare
  def initialize(*args)
    @log = args.first unless args.empty?
  end
  
  def call(session, message)
    @log << self.class if @log
    yield
  end
end

class MockMWare2 < MockMWare; end


class TorqueBox::Messaging::ProcessorMiddleware::MWare
  attr_reader :args
end

class TorqueBox::Messaging::ProcessorMiddleware::Chain
  def get_chain
    chain
  end
end
  
describe TorqueBox::Messaging::ProcessorMiddleware::Chain do
  before(:each) do
    @chain = TorqueBox::Messaging::ProcessorMiddleware::Chain.new
  end

  describe "#prepend" do
    it "should work when the chain is empty" do
      @chain.prepend(MockMWare)
      @chain.get_chain.first.klass.should == MockMWare
    end

    it "should work when the chain is not empty" do
      @chain.prepend(MockMWare)
      @chain.prepend(MockMWare2)
      @chain.get_chain.first.klass.should == MockMWare2
    end

    it "should not add the same class twice" do
      @chain.prepend(MockMWare)
      @chain.prepend(MockMWare)
      @chain.get_chain.first.klass.should == MockMWare
      @chain.get_chain.length.should == 1
    end

    it "should work with args" do
      @chain.prepend(MockMWare, :arg1, :arg2)
      mware = @chain.get_chain.first
      mware.args.should == [:arg1, :arg2]
    end

    it "should return the chain" do
      @chain.prepend(MockMWare).should == @chain
    end
    

  end

  describe "#append" do
    it "should work when the chain is empty" do
      @chain.append(MockMWare)
      @chain.get_chain.first.klass.should == MockMWare
    end

    it "should work when the chain is not empty" do
      @chain.append(MockMWare)
      @chain.append(MockMWare2)
      @chain.get_chain.last.klass.should == MockMWare2
    end

    it "should not add the same class twice" do
      @chain.append(MockMWare)
      @chain.append(MockMWare)
      @chain.get_chain.first.klass.should == MockMWare
      @chain.get_chain.length.should == 1
    end

    it "should return the chain" do
      @chain.append(MockMWare).should == @chain
    end

  end

  describe "#remove" do
    it "should work" do
      @chain.append(MockMWare)
      @chain.remove(MockMWare)
      @chain.get_chain.should be_empty
    end

    it "should not barf if the class isn't present" do
      lambda do
        @chain.remove(MockMWare)
      end.should_not raise_error
    end

    it "should only remove the given class" do
      @chain.append(MockMWare)
      @chain.append(MockMWare2)
      @chain.remove(MockMWare)
      @chain.get_chain.first.klass.should == MockMWare2
      @chain.get_chain.length.should == 1
    end

    it "should return the chain" do
      @chain.remove(MockMWare).should == @chain
    end

  end
  
  describe '#invoke' do
    before(:each) do 
      @processor = mock('processor')
      @log = []
      @processor.stub(:process!) do
        @log << :processor
      end.with("message") 
    end

    describe "when the chain is empty" do
      it "should call the processor" do
        @chain.invoke("session", "message", @processor)
      end
    end

    describe "when the chain is not empty" do      
      it "should call the processor" do
        @chain.append(MockMWare)
        @chain.invoke("session", "message", @processor)
      end

      it "should call the chain first" do
        @chain.append(MockMWare, @log)
        @chain.invoke("session", "message", @processor)
        @log.should == [MockMWare, :processor]
      end

      it "should call the chain in order" do
        @chain.append(MockMWare, @log)
        @chain.append(MockMWare2, @log)
        @chain.invoke("session", "message", @processor)
        @log.should == [MockMWare, MockMWare2, :processor]
      end
    end
  end

  describe "#inspect" do
    it "should work" do
      @chain.append(MockMWare)
      @chain.inspect.should == "[MockMWare]"
    end
  end
  
end

