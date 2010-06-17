require 'yaml'

require 'torquebox/messaging/destination_parser'

describe TorqueBox::Messaging::DestinationParser do

  it "should parse emptyness" do
    unit = mock('unit')
    TorqueBox::Messaging::DestinationParser.parse(unit, '')
  end

  it "should parse value YAML" do
    attachments = []
    destinations = {
      'foo'=>{
        :type=>:queue,
        :bind=>'/queues/foo',
        :durable=>true,
      }
    }
    yaml = YAML.dump( destinations )

    unit = mock('unit')
    unit.should_receive(:addAttachment) do |name, object|
      attachments << object
    end
    TorqueBox::Messaging::DestinationParser.parse(unit, yaml)
    attachments.should_not be_empty
    destination = attachments.first
    destination.name.should eql( "foo" )
    destination.durable?.should be_true
    destination.bind_name.should eql('/queues/foo' )
  end

end
