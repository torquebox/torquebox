
import org.torquebox.ruby.enterprise.client.RubyClient

module TorqueBox
  module Client
    
    def self.current()
      RubyClient.getClientForCurrentThread()      
    end
    
    def self.connect(app_name=JBoss.application_name, naming_host=RubyClient::DEFAULT_NAMING_HOST, naming_port=RubyClient::DEFAULT_NAMING_PORT)
      client = RubyClient.connect( app_name, naming_host, naming_port )      
      if block_given?
        begin
          yield client 
        ensure
          client.close
        end
      end
    end
    
  end
end