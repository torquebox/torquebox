
require 'torquebox/container/foundation'
require 'torquebox/naming/naming_service'
require 'torquebox/naming'

require 'socket'


describe TorqueBox::Naming::NamingService do

  describe 'basics' do
    before(:each) do
      @container = TorqueBox::Container::Foundation.new
      @service = TorqueBox::Naming::NamingService.new do |config|
        config.port, config.rmi_port = available_port(2)
      end
      @container.enable( @service )
      @container.start
    end

    after(:each) do
      @container.stop
    end

    it "should have a Naming bean" do
      naming_bean = @container['Naming']
      naming_bean.should_not be_nil
    end

    it "should have a JNDI bean" do
      jndi_bean = @container['JNDIServer']
      jndi_bean.should_not be_nil
    end
 
    it "should be reachable on port" do
     socket = Socket.new( Socket::Constants::AF_INET, Socket::Constants::SOCK_STREAM, 0 )
     sockaddr = Socket.pack_sockaddr_in( @service.port, 'localhost' )
     socket.connect( sockaddr )
     socket.close
    end

    it "should provide implicit default bindability" do
      TorqueBox::Naming['test'] = 'gouda'
      TorqueBox::Naming['test'].should eql( 'gouda' )
    end
 
    it "should provide explicit connected bindability" do
      TorqueBox::Naming.connect( 'localhost', @service.port ) do |context|
        context['test'] = 'gouda'
        context['test'].should eql( 'gouda' )
      end
    end
  end

  describe 'when not exported' do
    before(:each) do
      @container = TorqueBox::Container::Foundation.new
      @service = TorqueBox::Naming::NamingService.new do |config|
        config.port, config.rmi_port = available_port(2)
        config.export = false
      end
      @container.enable( @service )
      @container.start
    end

    after(:each) do
      @container.stop
    end

    it "should have a Naming bean" do
      naming_bean = @container['Naming']
      naming_bean.should_not be_nil
    end

    it "should not have a JNDI bean" do
      jndi_bean = @container['JNDIServer']
      jndi_bean.should be_nil
    end

    it "should not be reachable on port" do
     socket = Socket.new( Socket::Constants::AF_INET, Socket::Constants::SOCK_STREAM, 0 )
     sockaddr = Socket.pack_sockaddr_in( @service.port, 'localhost' )
     lambda{ socket.connect( sockaddr ) }.should raise_error
    end

    it "should provide implicit default bindability" do
      TorqueBox::Naming['test'] = 'gouda'
      TorqueBox::Naming['test'].should eql( 'gouda' )
    end
 
  end

end

def available_port count=1
  servers = (0...count).map{ TCPServer.new('127.0.0.1', 0)} 
  ports = servers.map {|s| s.addr[1]}
  servers.each {|s| s.close}
  count==1 ? ports.first : ports
end
