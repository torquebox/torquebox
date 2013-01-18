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


require 'java'

$: << File.dirname(__FILE__) + '/../lib' 

require 'torquebox-rake-support'

TESTING_ON_WINDOWS = ( java.lang::System.getProperty( "os.name" ) =~ /windows/i )
WINDOWS_ABSOLUTE_PREFIX = java.lang::System.getProperty("windows.absolute.prefix", "C:")

module PathHelper
  def self.extended(cls)
    cls.class_eval do

      def absolute_prefix
        self.class.absolute_prefix
      end

      def self.absolute_prefix
        return '' unless ( TESTING_ON_WINDOWS )
        WINDOWS_ABSOLUTE_PREFIX
      end
  
    end

  end
end

