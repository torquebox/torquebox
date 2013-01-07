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

require 'blankslate'
require 'torquebox/configuration/validator'

module TorqueBox
  module Configuration

    def self.load_configuration(file, config, entry_map)
      Thread.current[:torquebox_config] = config
      Thread.current[:torquebox_config_entry_map] = entry_map
      Dir.chdir( File.dirname( file ) ) do
        eval( File.read( file ) )
      end
      config
    end

    def self.const_missing(name)
      FakeConstant.new( name ).to_const
    end

    # @api private
    class Entry < BlankSlate
      def initialize(name, config, entry_map, options = { })
        @name = name
        @config = config
        @entry_map = entry_map
        @parents = options.delete( :parents ) || []
        @options = options
        @line_number = find_line_number
        @entry_options = { }
        
        if options[:require_parent] && ([options[:require_parent]].flatten & @parents).empty?
          raise ConfigurationError.new( "#{@name} only allowed inside #{options[:require_parent]}", @line_number )
        end
      end

      def find_line_number
        caller.each do |line|
          return $1 if line =~ /\(eval\):(\d+):/
        end
        nil
      end
      
      def process(*args, &block)
        process_args( args )
        eval_block( &block ) if block_given?
        validate_options
        finalize_options
        local_config
      end

      def process_args(unused)
        # no op
        @config
      end

      def validate_options
        if @options[:validate]
          validator = Validator.new( @options[:validate], @name, @entry_options )
          raise ConfigurationError.new( validator.message, @line_number ) unless validator.valid?
        end
      end

      def eval_block(&block)
        block.arity < 1 ? self.instance_eval( &block ) : block.call( self )
      end

      def self.const_missing(name)
        FakeConstant.new( name ).to_const
      end

      def self.with_settings(options)
        klass = self
        proxy = Object.new
        (class << proxy; self; end).__send__( :define_method, :new ) do |*args|
          if args.last.is_a?( Hash )
            args.last.merge!( options )
          else
            args << options
          end
          klass.new( *args )
        end
        proxy
      end

      alias_method :send, :__send__

      def method_missing(method, *args, &block)
        klass = @entry_map[method]
        if klass
          entry = klass.new( method, @entry_options, @entry_map, :parents => @parents + [@name] )
          entry.process( *args, &block )
        else
          add_options( method.to_sym => args.first )
        end
      end

      def add_options( option )
        @entry_options.merge!( option )
      end

      def finalize_options
        if @options[:discrete]
          local_config << @entry_options
        else
          @entry_options = local_config.merge!( @entry_options )
        end
        local_config
      end
      
      def local_config
        @config[@name.to_s] = [] if @options[:discrete] && !@config[@name.to_s].is_a?(Array)
        @config[@name.to_s] ||= {}  
      end

      def local_config=(value)
        @config[@name.to_s] = value
      end
    end

    # @api private
    class OptionsEntry < Entry
      def process_args(args)
        hash = args.first || { }
        raise ConfigurationError.new( "'#{@name}' takes a hash (and only a hash)", @line_number ) if !hash.is_a?(Hash) || args.length > 1
        add_options( hash )
      end
    end

    # @api private
    class ThingWithOptionsEntry < Entry
      def process_args(args)
        @thing, hash = args
        add_options( hash || {} )
      end

      def finalize_options
        if @options[:discrete]
          local_config << [@thing.to_s, @entry_options]
        else
          local_config[@thing.to_s] = { } unless local_config[@thing.to_s]
          @entry_options = local_config[@thing.to_s].merge!( @entry_options )
        end
      end
    end

    # @api private
    class ThingsEntry < Entry
      def process_args(args)
        @entry_options = args.map(&:to_s)
        local_config
      end
    end

    # @api private
    class Configuration < Hash
      def initialize
        super { |hash, key| hash[key] = { } }
      end
    end

    # @api private
    class FakeConstant
      def initialize(name)
        @name = name.to_s
        s = <<-END
          module ::#{name}
            def self.const_missing(k)
              FakeConstant.new( "#{name}::" + k.to_s ).to_const
            end
          end
        END

        eval s
 
      end

      def to_const
        eval @name
      end

      def to_s
        @name
      end
    end

    class ConfigurationError < RuntimeError
      def initialize(message, line_number = nil)
        message += " (line #{line_number})" if line_number
        super(message)
      end
    end

  end
end
