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
    # So sometimes the combination of phantomjs, poltergeist,
    # capybara, and this sockjs demo ends up flaking out and when that
    # happens we can tell because sockjs thinks it negotiated
    # xhr-polling instead of xhr-streaming. So, retry a few times to
    # make the test more stable.
    num_tries = 0
    begin
      visit "/sockjs.html"
      should_have_slow_content(page, '[*] open')
      num_tries += 1
      if page.has_content?('xhr-polling') && num_tries < 5
        page.reset!
        redo
      end
    end
    should_have_slow_content(page, 'message "Welcome!"')
    page.execute_script("inp.val('foobarbaz');form.submit();")
    should_have_slow_content(page, 'message "foobarbaz"')
  end

  def should_have_slow_content(page, expected_content)
    timeout = 30
    start = Time.now
    while (Time.now - start) < timeout
      break if page.source.include?(expected_content)
      sleep 0.2 # sleep and retry
    end
    page.should have_content(expected_content)
  end

end
