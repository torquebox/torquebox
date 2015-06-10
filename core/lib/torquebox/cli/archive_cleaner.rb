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

at_exit do
  # Clean up any temporary files left around by our jar unpacking
  # shenanigans. This is only used when executing a script inside
  # an archive, which ends up calling Kernel.exec and bypassing the
  # typical temp file cleaning mechanisms.
  java_tmpdir = File.expand_path(java.lang.System.get_property('java.io.tmpdir'))
  app_jar = java.lang.System.get_property('torquebox.app_jar')
  paths = $LOAD_PATH + [app_jar]
  path_pattern = %r{^(#{java_tmpdir}/wunderboss.+?/).+}
  paths.each do |path|
    path = File.expand_path(path)
    if path =~ path_pattern
      dir = $1
      if Dir.exist?(dir)
        FileUtils.rm_rf(dir)
      end
    end
  end
end
