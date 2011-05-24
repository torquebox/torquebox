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

describe "jaas authentication tests" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      RACK_ROOT: #{File.dirname(__FILE__)}/../apps/rack/jaas
      env: development

    web:
      context: /authentication

    ruby:
      version: #{RUBY_VERSION[0,3]}

    auth:
      test-jaas:
        domain: torquebox
  END

  it "should authenticate with proper credentials"# do
    #visit "/authentication/success"
    #page.should have_content('it worked')
  #end

  it "should authenticate as guest"# do
    #visit "/authentication/guest"
    #page.should have_content('it worked')
  #end

  it "should not authenticate with improper credentials"# do
    #visit "/authentication/failure"
    #page.should have_content('it failed')
  #end

end
