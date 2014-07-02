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

require 'torquebox/messaging/connection'
require 'torquebox/messaging/destination'
require 'torquebox/messaging/queue'
require 'torquebox/messaging/topic'
require 'torquebox/messaging/session'
require 'torquebox/messaging/hornetq'

module TorqueBox
  module Messaging
    def self.default_encoding
      @default_encoding ||= :marshal
    end

    def self.default_encoding=(enc)
      @default_encoding = enc
    end
  end
end
