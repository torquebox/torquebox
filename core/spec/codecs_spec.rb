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

def define_JSON
  klass = Class.new {
    def self.fast_generate(_)
    end
  }
  Object.const_set(:JSON, klass)
end

java_import org.projectodd.wunderboss.codecs.StringCodec

class FooCodec < StringCodec
  def initialize
    super("foo", "text/foo")
  end

  def encode(_)
    'foo_encode'
  end

  def decode(_)
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
      TorqueBox::Codecs['application/json'].should == TorqueBox::Codecs[:json]
    end

    context "requiring json" do
      before(:each) do
        Object.send(:remove_const, :JSON) if defined?(JSON)
      end

      it "should raise if json isn't available" do
        TorqueBox::Codecs[:json].should_receive(:require).with('json').and_raise(LoadError.new)
        lambda { TorqueBox::Codecs.encode('abc', :json) }.should raise_error( RuntimeError )
      end

      it "should not raise if json is available" do
        TorqueBox::Codecs[:json].should_receive(:require).with('json') { define_JSON }
        lambda { TorqueBox::Codecs.encode('abc', :json ) }.should_not raise_error
      end

      it "should only require json once" do
        TorqueBox::Codecs[:json].should_receive(:require).once.with('json') { define_JSON }
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
      TorqueBox::Codecs['application/edn'].should == TorqueBox::Codecs[:edn]
    end
  end

  context "text" do
    it "should decode what it encodes" do
      TorqueBox::Codecs.decode(TorqueBox::Codecs.encode('abc', :text), :text).should eql('abc')
    end

    it "should have the proper content-type" do
      TorqueBox::Codecs['text/plain'].should == TorqueBox::Codecs[:text]
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
      TorqueBox::Codecs['application/ruby-marshal'].should == TorqueBox::Codecs[:marshal]
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
      TorqueBox::Codecs['application/ruby-marshal-base64'].should == TorqueBox::Codecs[:marshal_base64]
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
      TorqueBox::Codecs['application/ruby-marshal-smart'].should == TorqueBox::Codecs[:marshal_smart]
    end
  end

  context "a custom codec" do
    it "should be registerable and findable" do
      TorqueBox::Codecs.add(FooCodec.new)
      TorqueBox::Codecs.encode('whatever', :foo).should == 'foo_encode'
      TorqueBox::Codecs.decode('whatever', :foo).should == 'foo_decode'

      TorqueBox::Codecs['text/foo'].should == TorqueBox::Codecs[:foo]
    end
  end
end
