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

require 'spec_helper'
require 'torquebox-security'

remote_describe "jaas" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    auth:
      global:
        domain: torquebox
      local:
        domain: torquebox-jaas
        credentials:
          scott: tiger
      pork:
        domain: pork
  END

  it "should authenticate against 'torquebox' with proper credentials" do
    authenticator = TorqueBox::Authentication[ 'global' ]
    authenticator.authenticate('scott', 'scott').should be_true
  end

  it "should authenticate against 'torquebox' as guest" do
    authenticator = TorqueBox::Authentication[ 'global' ]
    authenticator.authenticate('guest', nil).should be_true
  end

  it "should not authenticate against 'torquebox' with improper credentials" do
    authenticator = TorqueBox::Authentication[ 'global' ]
    authenticator.authenticate('foo', 'bar').should be_false
  end

  it "should authenticate against 'torquebox-jaas' with proper credentials" do
    authenticator = TorqueBox::Authentication[ 'local' ]
    authenticator.authenticate('scott', 'tiger').should be_true
  end

  it "should not authenticate against 'torquebox-jaas' with improper credentials" do
    authenticator = TorqueBox::Authentication[ 'local' ]
    authenticator.authenticate('foo', 'bar').should be_false
  end

  it "should fail to authenticate against domains configured in standalone.xml if credentials are invalid" do
    authenticator = TorqueBox::Authentication[ 'pork' ]
    authenticator.authenticate('crunchy', 'chicken').should be_false
  end

  it "should authenticate against domains configured in standalone.xml" do
    authenticator = TorqueBox::Authentication[ 'pork' ]
    authenticator.authenticate('crunchy', 'bacon').should be_true
  end

end
