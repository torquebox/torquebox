#
# Copyright 2011 Red Hat, Inc.
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
#

require 'dm-core/spec/shared/adapter_spec'
require 'dm-core/spec/lib/pending_helpers'
require 'cache'

RSpec::Runner.configure do |config|
  config.include(DataMapper::Spec::PendingHelpers)
end

def random_string( length = 20 )
  chars = ('a'..'z').to_a + ('A'..'Z').to_a
  dir_string = (0...length).collect { chars[Kernel.rand(chars.length)] }.join
end
