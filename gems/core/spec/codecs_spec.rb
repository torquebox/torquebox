require 'torquebox/codecs'
require 'torquebox/codecs/json'

def define_JSON
  klass = Class.new {
    def self.fast_generate(_)
    end
  }
  Object.const_set(:JSON, klass)
end

describe TorqueBox::Codecs do

  context "json" do
    it "should decode an encoded array" do
      TorqueBox::Codecs.decode(TorqueBox::Codecs.encode(['abc'], :json), :json).should eql(['abc'])
    end
    
    if RUBY_VERSION >= '1.9'
      it "should decode an encoded string" do
        TorqueBox::Codecs.decode(TorqueBox::Codecs.encode('abc', :json), :json).should eql('abc')
      end
    end
    
    context "requiring json" do
      before(:each) do
        Object.send(:remove_const, :JSON) if defined?(JSON)
      end

      it "should raise if json isn't available" do
        TorqueBox::Codecs::JSON.should_receive(:require).with('json').and_raise(LoadError.new)
        lambda { TorqueBox::Codecs.encode('abc', :json) }.should raise_error( RuntimeError )
      end

      it "should not raise if json is available" do
        TorqueBox::Codecs::JSON.should_receive(:require).with('json').and_return { define_JSON }
        lambda { TorqueBox::Codecs.encode('abc', :json ) }.should_not raise_error
      end

      it "should only require json once" do
        TorqueBox::Codecs::JSON.should_receive(:require).once.with('json').and_return { define_JSON }
        TorqueBox::Codecs.encode('abc', :json)
        TorqueBox::Codecs.encode('abc', :json)
      end
    end
  end

  context "edn" do
    it "should decode what it encodes" do
      TorqueBox::Codecs.decode(TorqueBox::Codecs.encode('abc', :edn), :edn).should eql('abc')
    end
  end

  context "marshal" do
    it "should decode what it encodes" do
      TorqueBox::Codecs.decode(TorqueBox::Codecs.encode('abc', :marshal), :marshal).should eql('abc')
    end

    it "should decode and encode Time objects" do
      now = Time.now
      TorqueBox::Codecs.decode(TorqueBox::Codecs.encode(now, :marshal), :marshal).should eql(now)
    end
  end

  context "marshal base64" do
    it "should decode what it encodes" do
      TorqueBox::Codecs.decode(TorqueBox::Codecs.encode('abc', :marshal_base64), :marshal_base64).should eql('abc')
    end

    it "should decode and encode Time objects" do
      now = Time.now
      TorqueBox::Codecs.decode(TorqueBox::Codecs.encode(now, :marshal_base64), :marshal_base64).should eql(now)
    end
  end

  context "marshal_smart" do
    it "should decode what it encodes" do
      TorqueBox::Codecs.decode(TorqueBox::Codecs.encode('abc', :marshal_smart), :marshal_smart).should eql('abc')
    end

    it "should decode and encode Time objects" do
      now = Time.now
      TorqueBox::Codecs.decode(TorqueBox::Codecs.encode(now, :marshal_smart), :marshal_smart).should eql(now)
    end
  end

end
