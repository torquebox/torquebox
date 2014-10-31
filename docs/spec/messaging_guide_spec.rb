
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

require "spec_helper"
require "torquebox-messaging"

describe "messaging guide" do

  it "should have valid code blocks" do
    new_method = TorqueBox::Messaging::Context.method(:new)
    TorqueBox::Messaging::Context.stub(:new) do |arg, &block|
      if arg
        if "some-host" == arg[:host]
          # stub the remote lookup to really go to localhost
          arg[:host] = "localhost"
          # and create the queue it expects to find
          TorqueBox::Messaging.queue("foo", :durable => false)
        end
        new_method.call(arg, &block)
      else
        new_method.call(&block)
      end
    end
    require 'messaging_guide'
  end
end
