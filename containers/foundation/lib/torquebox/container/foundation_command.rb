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
  module Container

    class FoundationCommand

      def initialize()
        @options_parser = OptionParser.new
        parser_options( @options_parser )
      end

      def parser_options(opts)
        opts.on( '-L', '--logging CONFIG_XML', 'Specify a logging configuration' ) do |logging_config_xml|
          @logging_config_xml = logging_config_xml
        end
        opts.on_tail( '-h', '--help', 'Show this message' ) do
          puts opts
          exit
        end
      end


      def run()
      
        full_path = File.expand_path( File.join( File.dirname( __FILE__ ), 'log4j.xml' ) )

        if ( @logging_config_xml )
          full_path = File.expand_path( @logging_config_xml ) 
        end

        org.apache.log4j.xml::DOMConfigurator.configure( full_path )

        container = TorqueBox::Container::Foundation.new
        configure( container )
        container.start
        after_start( container )

        interrupted = false
        trap( "INT" ) do
          before_stop( container )
          container.stop
          interrupted = true
        end

        while ( ! interrupted )
          sleep( 2 )
        end
      end

      def configure(container)
        # no op
      end

      def after_start(container)
      end

      def before_stop(container)
      end

      def parse!(args)
        @options_parser.parse!(args)
      end

    end

  end
end
