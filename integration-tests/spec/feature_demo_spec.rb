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

feature "feature demo app" do

  torquebox('--dir' => "#{apps_dir}/rack/feature-demo",
            '--context-path' => '/',
            '-e' => 'production')

  it 'should work for sinatra demo' do
    visit "/?it=worked"
    page.should have_content('it=worked')
  end

  it 'should work for sockjs demo' do
    visit "/sockjs.html"
    page.should have_content('[*] open')
    page.execute_script("inp.val('foobarbaz');form.submit();")
    expected_content = 'message "foobarbaz"'
    # Give some time for the submitted value to round-trip
    timeout = 10
    start = Time.now
    while (Time.now - start) < timeout
      break if page.source.include?(expected_content)
      sleep 0.2 # sleep and retry
    end
    page.should have_content(expected_content)
  end

end
