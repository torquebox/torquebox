require 'torquebox/codecs'
require 'torquebox/codecs/json'

def define_JSON
  klass = Class.new {
    def self.fast_generate(_)
    end
  }
  Object.const_set(:JSON, klass)
end

class FooCodec
  def self.encode(_)
    'foo_encode'
  end

  def self.decode(_)
    'foo_decode'
  end
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

    it "should have the proper content-type" do
      TorqueBox::Codecs.content_type_for_name(:json).should == 'application/json'
      TorqueBox::Codecs.name_for_content_type('application/json').should == :json
    end

    it "should not be binary" do
      TorqueBox::Codecs.binary_content?(:json).should == false
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

    it "should have the proper content-type" do
      TorqueBox::Codecs.content_type_for_name(:edn).should == 'application/edn'
      TorqueBox::Codecs.name_for_content_type('application/edn').should == :edn
    end

    it "should not be binary" do
      TorqueBox::Codecs.binary_content?(:edn).should == false
    end
  end

  context "text" do
    it "should decode what it encodes" do
      TorqueBox::Codecs.decode(TorqueBox::Codecs.encode('abc', :text), :text).should eql('abc')
    end

    it "should have the proper content-type" do
      TorqueBox::Codecs.content_type_for_name(:text).should == 'text/plain'
      TorqueBox::Codecs.name_for_content_type('text/plain').should == :text
    end

    it "should not be binary" do
      TorqueBox::Codecs.binary_content?(:text).should == false
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

    it "should have the proper content-type" do
      TorqueBox::Codecs.content_type_for_name(:marshal).should == 'application/ruby-marshal'
      TorqueBox::Codecs.name_for_content_type('application/ruby-marshal').should == :marshal
    end

    it "should not be binary" do
      TorqueBox::Codecs.binary_content?(:marshal).should == false
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

    it "should have the proper content-type" do
      TorqueBox::Codecs.content_type_for_name(:marshal_base64).should == 'application/ruby-marshal-base64'
      TorqueBox::Codecs.name_for_content_type('application/ruby-marshal-base64').should == :marshal_base64
    end

    it "should not be binary" do
      TorqueBox::Codecs.binary_content?(:marshal_base64).should == false
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

    it "should have the proper content-type" do
      TorqueBox::Codecs.content_type_for_name(:marshal_smart).should == 'application/ruby-marshal-smart'
      TorqueBox::Codecs.name_for_content_type('application/ruby-marshal-smart').should == :marshal_smart
    end

    it "should not be binary" do
      TorqueBox::Codecs.binary_content?(:marshal_smart).should == false
    end
  end

  context "a custom codec" do
    it "should be registerable and findable" do
      TorqueBox::Codecs.register_codec(:foo, 'text/foo', FooCodec)
      TorqueBox::Codecs.encode('whatever', :foo).should == 'foo_encode'
      TorqueBox::Codecs.decode('whatever', :foo).should == 'foo_decode'

      TorqueBox::Codecs.content_type_for_name(:foo).should == 'text/foo'
      TorqueBox::Codecs.name_for_content_type('text/foo').should == :foo
    end
  end
end
