# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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

module TorqueBox
  module VFS
    class GlobTranslator

      ESCAPED_CHARS = [ '.', '?', '(', ')', '[', ']' ]
      
      def initialize(input)
        @input      = input
        @cur        = 0
        @alternation_depth = 0
      end
      
      
      def self.translate(glob_str)
        translator = GlobTranslator.new( glob_str )
        regexp_str = translator.glob()
        #puts "#{glob_str} ==> #{regexp_str}"
        regexp_str
      end

      def self.to_regexp(glob_str)
        regexp_str = translate( glob_str )
        ##puts "regexp_str: #{regexp_str}"
        Regexp.new( regexp_str )
      end
      
      def glob()
        result = ''
        while ( ! complete? )
          c = la()
          case( c )
          when '*'
            result += splat()
          when '?'
            result += question()
          when '{'
            result += alternation()
          when '['
            result += char_class()
          else
            if ( la_is_char? )
              result += char();
            else
              break
            end
          end
        end
        result 
      end
      
      def splat()
        #puts "enter splat()"
        if ( la(1) == '*' )
          result = double_splat()
        else
          if ( cur() == 0 || lb() == '/' )
            result = '[^/.][^/]*'
          else
            result = '[^/]+'
          end
          consume('*')
        end
        #puts "exit splat()"
        result
      end

      def double_splat()
        #puts "enter double_splat()"
        result = '(.*)'
        consume('*')
        consume('*')
        consume('/') if ( la() == '/' )
        #puts "exit double_splat()"
        result
      end
      
      def question()
        #puts "enter question()"
        consume('?')
        #puts "exit question()"
        '[^/]'
      end 
      
      def alternation()
        #puts "enter alternation()"
        @alternation_depth += 1
        
        result = '('
        
        match_empty = false
        
        consume('{')
        if ( la() == ',' )
          consume(',')
          match_empty = true
        end
        
        result += '(' + glob() +')' 
        
        while ( la() == ',' )
          consume(',')
          if ( la() == '}' )
            match_empty = true
            break
          end
          result += '|(' + glob() + ')'
        end
        
        if ( match_empty )
          result += '|'
        end
        result += ')'
        consume( '}' )
        @alternation_depth -= 1
        #puts "exit alternation()"
        result
      end

      def char_class()
        consume( '[' )
        result = '['

        while( la() != ']' )
          result += consume();
        end

        consume( ']' )
        result += ']'
        result
      end
      
      def char()
        #puts "enter char()"
        c = consume() 
        if ( c == '\\' )
          c = consume()
        end
        if ( ESCAPED_CHARS.include?( c  ) )
          c = '\\' + c
        end
        #puts "exit char() #{c}"
        c
      end

      def la_is_char?()
        ( return true ) if ( @alternation_depth == 0 )
        case ( la() )
        when '}'
          return false
        when ','
          return false
        else
          return true
        end
      end
      
      private
      
      def la(a=0)
        @input[@cur+a,1]
      end

      def lb()
        @input[(@cur-1),1]
      end

      def cur()
        @cur
      end
      
      def consume(expected=nil)
        c = la()
        #puts "attempt consume: #{c}"
        raise "Unexpected character: '#{c}'" if ( ( ! expected.nil? ) && ( c != expected ) )
        @cur += 1
        c
      end
      
      def complete?
        @cur >= @input.length
      end
      
    end

  end
end

