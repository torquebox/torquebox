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

require "fileutils"
require "rexml/document"
require "open3"

module TorqueBox
  class IncrementalTasks
    extend Rake::DSL if defined? Rake::DSL

    class << self
      def install
        task "publish_incremental" do
          base_url = "https://repository-projectodd.forge.cloudbees.com/rubygems/4x"
          credentials_path = ENV["CREDENTIALS_PATH"]
          unless credentials_path
            $stderr.puts "Error: You must specify $CREDENTIALS_PATH"
            exit 1
          end
          dav = DAV.new(credentials_path)

          FileUtils.mkdir_p("pkg/gem-repo/gems")
          dav.mkcol("#{base_url}/gems")
          Dir.chdir("pkg/gem-repo") do
            puts `wget -nv -r -nH --cut-dirs=2 --no-parent --reject "index.html*" #{base_url}/gems/`
          end

          Dir.glob("**/pkg/torquebox*.gem").each do |gem_path|
            FileUtils.cp(gem_path, "pkg/gem-repo/gems/")
            gem = File.basename(gem_path)
            dav.put("#{base_url}/gems/#{gem}", "pkg/gem-repo/gems/#{gem}")
          end
          puts `#{Gem.ruby} -S gem generate_index -d pkg/gem-repo`
          Dir.chdir("pkg/gem-repo") do
            Dir.glob("**/*[^.gem]").each do |file|
              if (File.directory?(file))
                dav.mkcol("#{base_url}/#{file}")
              else
                dav.put("#{base_url}/#{file}", file)
              end
            end
          end
        end
      end
    end

    class DAV

      def initialize(credentials_path)
        load_credentials(credentials_path)
      end

      def load_credentials(credentials_path)
        text = File.read(credentials_path)
        doc = REXML::Document.new(text)
        @username = doc.get_elements("//servers/server/username").first.text
        @password = doc.get_elements("//servers/server/password").first.text
      end

      def mkcol(url)
        status, message = curl(
          "--request MKCOL",
          "--header 'Content-Type: text/xml; charset=\"utf-8\"'",
          url
        )
      end

      def put(url, file)
        status, message = curl(
          "--upload-file",
          file,
          url
        )
      end

      def delete(url)
        status, message = curl(
          "--request DELETE",
          "--header 'Content-Type: text/xml; charset=\"utf-8\"'",
          url
        )
      end

      def copy(src, dest, depth)
        status, message = curl(
          "--request COPY",
          "--header 'Destination: #{dest}'",
          "--header 'Depth: #{depth}'",
          "--header 'Overwrite: T'",
          src
        )
      end

      def curl(*args)
        cmd = "curl -v -s -u#{@username}:#{@password} #{args.join(' ')}"
        puts "CMD: #{args.join(' ')}"
        response = ""
        error    = ""
        Open3.popen3(cmd) do |stdin, stdout, stderr|
          stdin.close
          stdout_thr = Thread.new(stdout) do |stream|
            while !stream.eof?
              response += stream.readline
            end
          end
          stderr_thr = Thread.new(stderr) do |stream|
            while !stream.eof?
              error += stream.readline
            end
          end
          stdout_thr.join
          stderr_thr.join
        end
        lines = error.split("\n").find { |e| e =~ /^< HTTP\/1.1/ }
        status_line = (error.split("\n").find { |e| e =~ /^< HTTP\/1.1/ }) || ""
        status  = 500
        message = "Unknown"
        if (status_line =~ /HTTP\/1.1 ([0-9][0-9][0-9]) (.*)$/)
          status = $1
          message = $2
        end
        [status, message]
      end

    end

  end
end
