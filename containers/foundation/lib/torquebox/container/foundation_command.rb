
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
