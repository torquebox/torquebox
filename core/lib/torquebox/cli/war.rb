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

require 'torquebox-core'

module TorqueBox
  class CLI
    class War < Jar

      def run(argv, options)
        jar_options = options.dup
        jar_options.delete('jar_name')
        jar_name = super(argv, jar_options)
        war_name = options['jar_name'] || "#{File.basename(Dir.pwd)}.war"
        war_builder = org.torquebox.core.JarBuilder.new

        war_builder.add_string('WEB-INF/web.xml',
                               read_base_xml('web.xml'))
        war_builder.add_string('WEB-INF/jboss-deployment-structure.xml',
                               read_base_xml('jboss-deployment-structure.xml'))
        war_builder.add_file("WEB-INF/lib/#{jar_name}", jar_name)

        if File.exist?(war_name)
          @logger.info("Removing {}", war_name)
          FileUtils.rm_f(war_name)
        end
        @logger.info("Writing {}", war_name)
        war_builder.create(war_name)
      end

      protected

      def read_base_xml(name)
        java.lang.Thread.current_thread
          .context_class_loader
          .resource_as_string("base-xml/#{name}")
      end

    end
  end
end

TorqueBox::CLI.register_extension('war', TorqueBox::CLI::War.new,
                                  'Create a deployable war from an application')
