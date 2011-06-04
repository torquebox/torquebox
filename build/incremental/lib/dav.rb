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

  def curl(*args)
    cmd = "curl -v -s -u#{@username}:#{@password} #{args.join(' ')}"
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
    status_line = error.split( "\n" ).find{|e| e =~ /^< HTTP\/1.1/}.first
    status  = 500
    message = 'Unknown'
    if ( status_line =~ /HTTP\/1.1 ([0-9][0-9][0-9]) (.*)$/ ) 
      status = $1
      message = $2
    end
    [ status, message ]
  end

end
