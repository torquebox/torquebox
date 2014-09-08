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

      def option_defaults
        super.merge(:war_name => "#{File.basename(Dir.pwd)}.war")
      end

      def available_options
        super
          .reject { |v| v[:name] == :jar_name }
          .unshift(:name => :war_name,
                   :switch => '--name NAME',
                   :description => "Name of the war file (default: #{option_defaults[:war_name]})")
      end

      def run(argv, options)
        options = option_defaults.merge(options)
        jar_options = options.dup
        jar_options.delete(:destination)
        jar_path = super(argv, jar_options)
        begin
          war_path = File.join(options[:destination], options[:war_name])
          war_builder = org.torquebox.core.JarBuilder.new

          war_builder.add_string('WEB-INF/web.xml',
                                 read_base_xml('web.xml'))
          war_builder.add_string('WEB-INF/jboss-deployment-structure.xml',
                                 read_base_xml('jboss-deployment-structure.xml'))
          war_builder.add_file("WEB-INF/lib/#{File.basename(jar_path)}", jar_path)

          if File.exist?(war_path)
            @logger.info("Removing {}", war_path)
            FileUtils.rm_f(war_path)
          end
          @logger.info("Writing {}", war_path)
          war_builder.create(war_path)
        ensure
          @logger.info("Removing {}", jar_path)
          FileUtils.rm_f(jar_path)
        end
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
