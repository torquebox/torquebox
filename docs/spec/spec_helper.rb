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

CORE_DIR = "#{File.dirname(__FILE__)}/../../core"
WEB_DIR = "#{File.dirname(__FILE__)}/../../web"
MESSAGING_DIR = "#{File.dirname(__FILE__)}/../../messaging"
$LOAD_PATH << "#{CORE_DIR}/lib"
$LOAD_PATH << "#{WEB_DIR}/lib"
$LOAD_PATH << "#{MESSAGING_DIR}/lib"
require "#{CORE_DIR}/spec/spec_helper"

require 'yaml'

RSpec.configure do |config|
  config.before(:suite) do
    pkg_dir = "#{File.dirname(__FILE__)}/../pkg"
    FileUtils.mkdir_p(pkg_dir)
    guides = YAML.load_file("#{File.dirname(__FILE__)}/../guides.yml")
    guides.each do |guide|
      contents = File.readlines("#{File.dirname(__FILE__)}/../#{guide}.md")
      code = contents.select { |line| line.start_with?("    ") }
      code = code.map { |line| line.sub(/^    /, '') }
      File.open("#{pkg_dir}/#{guide}_guide.rb", "w") do |file|
        file.write(code.join)
      end
    end
    $LOAD_PATH << pkg_dir
  end

  config.after(:suite) do
    Java::OrgProjectoddWunderboss::WunderBoss.shutdown_and_reset
    FileUtils.rm_rf("#{File.dirname(__FILE__)}/../hornetq-data")
  end
end
