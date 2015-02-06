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

module Foo
  class Bar
  end
end

describe TorqueBox::Logger do

  let(:logger) { TorqueBox::Logger.new }

  context "simple logging" do
    it "should log trace" do
      logger.trace("foo")
    end

    it "should log debug" do
      logger.debug("foo")
    end

    it "should log info" do
      logger.info("foo")
    end

    it "should log warn" do
      logger.warn("foo")
    end

    it "should log error" do
      logger.error("foo")
    end

    it "should log fatal" do
      logger.fatal("foo")
    end
  end

  context "parameterized logging" do
    it "should format one parameter" do
      logger.info("foo {}", "bar")
    end

    it "should format two parameters" do
      logger.info("foo {} {}", "bar", "baz")
    end
  end

  context "log levels" do
    it "should respect global log level" do
      TorqueBox::Logger.log_level = 'WARN'
      logger = TorqueBox::Logger.new
      logger.level.should == 'WARN'
      logger.info?.should be_falsey
      logger.warn?.should be_truthy
    end
  end

  context "torquebox 3 specs" do
    it "should look nice for class objects" do
      logger = TorqueBox::Logger.new(Foo::Bar)
      logger.error("JC: log for cache store")
    end

    it "should support the various boolean methods" do
      logger.should respond_to(:trace?)
      logger.should respond_to(:debug?)
      logger.should respond_to(:info?)
      logger.should respond_to(:warn?)
      logger.should respond_to(:error?)
      logger.should respond_to(:fatal?)
    end

    # it "should not barf on meaningless level setting" do
    #   skip
    #   logger.level = Logger::WARN
    #   logger.level.should == Logger::WARN
    # end

    it "should deal with blocks correctly" do
      logger.error "JC: message zero"
      logger.error { "JC: message" }
      logger.error "JC: message too"
    end

    it "should handle nil parameters" do
      logger.info(nil)
    end

    it "should support the rack.errors interface" do
      logger.should respond_to(:puts)
      logger.should respond_to(:write)
      logger.should respond_to(:flush)
    end

    # it "should have a formatter" do
    #   skip
    #   logger.should respond_to(:formatter)
    #   logger.formatter.should_not be_nil
    # end
  end
end
