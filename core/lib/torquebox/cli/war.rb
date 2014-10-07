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

      def usage_parameters
        "[options] [rackup_file]"
      end

      def option_defaults
        super.merge(:war_name => "#{File.basename(Dir.pwd)}.war")
      end

      def available_options
        super
          .reject { |v| v[:name] == :jar_name }
          .unshift(:name => :war_name,
                   :switch => '--name NAME',
                   :description => "Name of the war file (default: #{option_defaults[:war_name]})")
          .push(:name => :resource_paths,
                :switch => '--resource-paths PATHS',
                :description => "Paths whose contents will be included at the top-level of the war\
 (default: none)",
                :type => Array)
          .push(:name => :context_path,
                :switch => '--context-path PATH',
                :description => "Deploys the war to the given context path (default: the name of\
 the war)")
          .push(:name => :virtual_host,
                :switch => '--virtual-host HOST',
                :description => "Deploys the war to the named host defined in the WildFly config\
 (default: none)")
          .push(:name => :env,
                :switch => '--env ENVIRONMENT',
                :short => '-E',
                :description => "Environment to run under (default: development)")
      end

      def run(argv, options)
        unless argv.empty?
          options[:rackup] = argv.shift
        end
        options = option_defaults.merge(options)
        if options[:env]
          options[:envvar]['RACK_ENV'] = options[:env]
          options[:envvar]['RAILS_ENV'] = options[:env]
        end
        jar_options = options.dup
        jar_options.delete(:destination)
        jar_path = super(argv, jar_options)
        begin
          war_path = File.join(options[:destination], options[:war_name])
          war_builder = org.torquebox.core.JarBuilder.new

          (options[:resource_paths] || []).each do |path|
            @logger.info("Copying contents of {} to war", path)
            add_files(war_builder,
                      :file_prefix => path,
                      :pattern => "**/*")
          end

          add_web_xml(war_builder)
          add_jboss_deployment_structure_xml(war_builder)
          add_jboss_web_xml(war_builder, options)

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

      def add_web_xml(war_builder)
        unless war_builder.has_entry('WEB-INF/web.xml')
          war_builder.add_string('WEB-INF/web.xml',
                                 read_base_xml('web.xml'))
        end
      end

      def add_jboss_deployment_structure_xml(war_builder)
        unless war_builder.has_entry('WEB-INF/jboss-deployment-structure.xml')
          war_builder.add_string('WEB-INF/jboss-deployment-structure.xml',
                                 read_base_xml('jboss-deployment-structure.xml'))
        end
      end

      def add_jboss_web_xml(war_builder, options)
        jboss_web = 'WEB-INF/jboss-web.xml'
        context_path = options[:context_path]
        virtual_host = options[:virtual_host]

        if context_path || virtual_host
          if war_builder.has_entry(jboss_web)
            @logger.warn("context-path or virtual-host specified, but a #{jboss_web} exists in\
 resource-paths. Ignoring options.")
          else
            root_el = context_path ? "  <context-root>#{context_path}</context-root>\n" : ''
            host_el = virtual_host ? "  <virtual-host>#{virtual_host}</virtual-host>\n" : ''
            war_builder.add_string(jboss_web,
                                   "<jboss-web>\n#{root_el}#{host_el}</jboss-web>")
          end
        end
      end

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
