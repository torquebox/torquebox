require 'yaml'

module TorqueBox
  module Messaging
    class DestinationParser

      def self.parse_path(unit, path)
        self.parse( unit, File.read( path ) )
      end

      def self.parse(unit, contents)
        metadata = []
        yaml = YAML.load( contents )
        if ( yaml )
          yaml.each do |name, config|
            destination = nil
            case ( config[:type].to_s )
              when 'topic'
                destination = org.torquebox.messaging.metadata::TopicMetaData.new( name.to_s )
              else
                destination = org.torquebox.messaging.metadata::QueueMetaData.new( name.to_s )
            end
            if ( config[:durable] )
              destination.durable = true
            end
            if ( config[:bind] )
              destination.bind_name = config[:bind].to_s
            end
 
            puts "destination #{destination}"
            org.torquebox.mc::AttachmentUtils.multipleAttach( unit, destination, name.to_s )
          end
        end
        metadata
      end

    end
  end
end
