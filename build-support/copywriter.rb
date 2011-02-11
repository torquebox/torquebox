#!/usr/bin/env ruby

require 'find'
require 'fileutils'

COPYRIGHT_STATEMENT = <<END
Copyright 2008-2011 Red Hat, Inc, and individual contributors.

This is free software; you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation; either version 2.1 of
the License, or (at your option) any later version.

This software is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this software; if not, write to the Free
Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
02110-1301 USA, or see the FSF site: http://www.fsf.org.
END


PRUNE_DIRS = [ 'target', 'tmp', '.git' ]

def comment_wrap(text, comment_line, comment_begin=nil, comment_end=nil)
  wrapped = ""
  wrapped << "#{comment_begin}\n" if comment_begin
  text.split("\n").each do |line|
    wrapped << "#{comment_line} #{line}\n"
  end
  wrapped << "#{comment_end}\n" if comment_end
  wrapped
end

LANGUAGE_COMMENT_DELIMS = {
  :java => [ ' *', '/*', ' */' ],
  :xml  => [ '    ', '<!--', '-->' ],
  :ruby => [ '#' ],
}

COPYRIGHT_STATEMENTS = {
  :java => comment_wrap( COPYRIGHT_STATEMENT, *LANGUAGE_COMMENT_DELIMS[:java] ),
  :xml  => comment_wrap( COPYRIGHT_STATEMENT, *LANGUAGE_COMMENT_DELIMS[:xml]  ),
  :ruby => comment_wrap( COPYRIGHT_STATEMENT, *LANGUAGE_COMMENT_DELIMS[:ruby] ),
}

def header(lang)
  COPYRIGHT_STATEMENTS[lang]
end

def project_dirs
  dirs = []
  Find.find( '.' ) do |path|
    basename = File.basename( path )
    Find.prune if ( PRUNE_DIRS.include?( basename ) )
    dirs << File.dirname( path ) if ( basename == 'pom.xml' )
  end
  dirs
end

def copywrite_dir(dir, lang, glob)
  Dir[ File.join( dir, glob ) ].each do |file|
    copywrite_file( file, lang )
  end
end

def copywrite_file(file, lang)
  bak_file = file + '.bak'
  FileUtils.cp( file, bak_file ) 
  File.open( bak_file, 'r' ) do |input_file|
    #output_file = $stdout
    File.open( file, 'w' ) do |output_file|
      skip_header_comment(input_file, lang)
      output_file.puts header(lang)
      output_file.puts "\n"
      input_file.each_line do |line|
        output_file.puts line
      end
    end
  end
  FileUtils.rm( bak_file )
end

def skip_header_comment(input, lang)
  delims = LANGUAGE_COMMENT_DELIMS[lang]
  skip_blank_lines( input )
  if ( delims.size == 1 )
    skip_simple_header_comment( input, *delims )
  else
    skip_block_header_comment( input, *delims )
  end
  skip_blank_lines( input )
end

def skip_blank_lines(input)
  while ( ! input.eof? )
    pos = input.pos
    line = input.readline
    if ( line.strip != '' )
      input.seek( pos )
      input.seek( pos )
      return
    end
  end
end

def skip_simple_header_comment(input, comment_line)
  while ( true )
    pos = input.pos
    line = input.readline
    next if ( line.strip =~ /^#{Regexp.escape(comment_line.strip)}/ )
    input.seek( pos ) 
    return
  end 
end

def skip_block_header_comment(input, comment_line, comment_begin, comment_end )
  state = :begin
  while ( true )
    pos = input.pos
    line = input.readline
    case ( state )
      when :begin
        if ( line.strip =~ /^#{Regexp.escape(comment_begin.strip)}/ )
          return if ( line.strip =~ /#{Regexp.escape(comment_end.strip)}$/ )
          state = :skip
          next
        else
          input.seek( pos )
          return
        end
      when :skip
        if ( line.strip =~ /#{Regexp.escape(comment_end.strip)}$/ )
          return
        end
    end
  end
  return
end

project_dirs.each do |dir|
  $stderr.puts "Copywriting: #{dir}"
  copywrite_dir( dir, :java, "src/*/java/**/*.java" )
  copywrite_dir( dir, :ruby, "src/*/java/**/*.rb" )
  copywrite_dir( dir, :ruby, "lib/**/*.rb" )
end

#copywrite_file( './components/base/base-spi/src/main/java/org/torquebox/interp/spi/RubyRuntimeFactory.java', :java )
#copywrite_file( './components/rails/rails-core/src/main/java/org/torquebox/rails/core/as_logger.rb', :ruby )



