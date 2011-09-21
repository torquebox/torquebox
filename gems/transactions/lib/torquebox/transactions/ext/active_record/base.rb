
module ActiveRecord
  class Base

    class << self
      include TorqueBox::Injectors

      alias_method :configurations_before_torquebox, :configurations

      def configurations
        xa_configs = configurations_before_torquebox.dup
        inject( 'xa-ds-info' ).configurations.each do |config|
          xa_configs[ config.name ][ 'jndi' ] = config.jndi_name
        end 
        xa_configs 
      end
    end
  end
end

