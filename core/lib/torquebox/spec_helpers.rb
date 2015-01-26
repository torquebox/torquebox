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

require 'fileutils'
require 'tempfile'

module TorqueBox
  # @api private
  class SpecHelpers

    def self.boot_marker_env_key
      "TORQUEBOX_SPEC_BOOT_MARKER"
    end

    def self.set_boot_marker
      boot_marker = Tempfile.new('tb_spec_boot_marker')
      ENV[boot_marker_env_key] = boot_marker.path
      boot_marker.close
      boot_marker.unlink
    end

    def self.clear_boot_marker
      if ENV[boot_marker_env_key]
        FileUtils.rm_f(ENV[boot_marker_env_key])
        ENV[boot_marker_env_key] = nil
      end
    end

    def self.booted
      File.open(ENV[boot_marker_env_key], "w") {} if ENV[boot_marker_env_key]
    end

    def self.booted?
      File.exist?(ENV[boot_marker_env_key])
    end
  end
end
