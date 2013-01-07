# Copyright 2008-2013 Red Hat, Inc, and individual contributors.
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

require 'torquebox/configuration'

module TorqueBox
  module Configuration
    # @api private
    class Validator
      
      def initialize(ruleset, entry, options_to_validate)
        @required = ruleset[:required] || []
        @allowed = @required + (ruleset[:optional] || [])
        @entry = entry
        @options_to_validate = options_to_validate
        validate
      end


      def valid?
        !message
      end

      def message
        case messages.size
        when 0
          nil
        when 1
          messages.first
        else
          result = "There are multiple messages for this entry:"
          messages.each { |message| result << "\n  " << message }
          result
        end
      end
      
      protected
      def messages
        @messages ||= []
      end
      
      def validate
        validate_required
        validate_allowed
        validate_values
      end

      def validate_required
        keys_for( @required ).each do |required|
          messages << "Required option :#{required} is missing on '#{@entry}'" unless @options_to_validate.keys.map(&:to_s).include?( required )
        end
      end

      def validate_allowed
        @options_to_validate.keys.map(&:to_s).each do |option|
          messages << "Option :#{option} is not allowed on '#{@entry}'" unless keys_for( @allowed ).include?( option )
        end
      end

      def validate_values
        @allowed.each do |allowed|
          if allowed.is_a?( Hash )
            key = allowed.keys.first
            if @options_to_validate.has_key?( key ) && !allowed[key].include?( @options_to_validate[key] )
              messages << "#{@options_to_validate[key].inspect} is not a valid value for #{key.inspect} on '#{@entry}'. Valid values are #{allowed[key].inspect}"
            end
          end
        end
      end
      
      def keys_for(ary)
        ary.collect do |value|
          (value.is_a?( Hash ) ? value.keys.first : value).to_s
        end
      end
    end
  end
end
