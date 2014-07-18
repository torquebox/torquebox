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
        web_xml = <<-EOS
<web-app xmlns="http://java.sun.com/xml/ns/javaee"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
      version="3.0">
  <distributable />
  <listener>
    <listener-class>
      org.projectodd.wunderboss.wildfly.ServletListener
    </listener-class>
  </listener>
</web-app>
EOS
        war_builder.add_string('WEB-INF/web.xml', web_xml)
        war_builder.add_file("WEB-INF/lib/#{jar_name}", jar_name)

        if File.exists?(war_name)
          @logger.infof("Removing %s", war_name)
          FileUtils.rm_f(war_name)
        end
        @logger.infof("Writing %s", war_name)
        war_builder.create(war_name)
      end
    end
  end
end

TorqueBox::CLI.register_extension('war', TorqueBox::CLI::War.new,
                                  'Create a deployable war from an application')
