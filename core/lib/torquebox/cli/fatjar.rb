module TorqueBox
  class CLI
    class FatJar

      def usage_parameters
        ""
      end

      def setup_parser(parser, options)
      end

      def run(argv, options)
      end
    end
  end
end

TorqueBox::CLI.register_extension('fatjar', TorqueBox::CLI::FatJar.new,
                                  'Create an executable "fatjar"')
