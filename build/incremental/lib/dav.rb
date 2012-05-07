# Copyright 2008-2012 Red Hat, Inc, and individual contributors.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

require 'rexml/document'
require 'open3'

class DAV

  def initialize(credentials_path)
    load_credentials(credentials_path)
  end

  def load_credentials(credentials_path)
    text = File.read( credentials_path )
    doc = REXML::Document.new( text )
    @username = doc.get_elements( '//servers/server/username' ).first.text
    @password = doc.get_elements( '//servers/server/password' ).first.text
  end

  def mkcol(url)
    status, message = curl(
      '--request MKCOL',
      "--header 'Content-Type: text/xml; charset=\"utf-8\"'",
      url
    )
  end

  def put(url, file)
    status, message = curl(
      '--upload-file', 
      file,
      url
    )
  end

  def delete(url)
    status, message = curl(
      '--request DELETE',
      "--header 'Content-Type: text/xml; charset=\"utf-8\"'",
      url
    )
  end

  def copy(src, dest, depth)
    status, message = curl(
      '--request COPY',
      "--header 'Destination: #{dest}'",
      "--header 'Depth: #{depth}'",
      "--header 'Overwrite: T'",
      src
    )
  end

  def curl(*args)
    cmd = "curl -v -s -u#{@username}:#{@password} #{args.join(' ')}"
    puts "CMD: #{args.join(' ')}"
    response = ''
    error    = ''
    Open3.popen3( cmd ) do |stdin, stdout, stderr|
      stdin.close
      stdout_thr = Thread.new(stdout) do |stream|
        while ( ! stream.eof? )
          response += stream.readline
        end
      end
      stderr_thr = Thread.new(stderr) do |stream|
        while ( ! stream.eof? )
          error += stream.readline
        end
      end
      stdout_thr.join
      stderr_thr.join
    end
    lines = error.split( "\n" ).find{|e| e =~ /^< HTTP\/1.1/}
    status_line = (error.split( "\n" ).find{|e| e =~ /^< HTTP\/1.1/}) || ''
    status  = 500
    message = 'Unknown'
    if ( status_line =~ /HTTP\/1.1 ([0-9][0-9][0-9]) (.*)$/ ) 
      status = $1
      message = $2
    end
    [ status, message ]
  end

end
