
module TorqueBox
  module Messaging
    module MetaData
      class Builder

        attr_reader :processors

        def initialize(&block)
          @processors = []
          block.call( self ) if block
        end
        
        def evaluate_file(file)
          evaluate( File.read( file ) ) 
        end
        
        def evaluate(config)
          @processors = Java::org.torquebox.messaging.deployers::MessagingYamlParsingDeployer::Parser.parse(config)
        end

        def self.evaluate_file(file)
          builder = Builder.new()
          builder.evaluate_file( file )
          builder.processors
        end

      end
    end
  end
end

