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

require 'torquebox/naming/ext/javax_naming_context'
require 'torquebox/logger'

module TorqueBox
  module Naming

    REMOTE_NAMING_FACTORY = 'org.jboss.naming.remote.client.InitialContextFactory'
    LOCAL_NAMING_FACTORY = 'org.jboss.as.naming.InitialContextFactory'

    # Connects to a remote server and returns (or yields) InitialContext for
    # lookups.
    #
    # @note If you use this method without providing a block, make sure you
    #       close the context after usage (use <tt>close</tt> method).
    #
    # @note JBoss AS 7.1+ provides the <tt>java:jboss/exported</tt> context,
    #       entries bound to this context are accessible over remote JNDI.
    #       No other objects will be available for remote lookups.
    #       This means that if you want to have a JNDI object available
    #       for remote lookup you need to export it first. For more information
    #       please refer to the JBoss AS 7 wiki:
    #       https://docs.jboss.org/author/display/AS71/JNDI+Reference
    def self.remote_context(options = {}, &block)
      ctx = javax.naming::InitialContext.new(populate_properties(options.merge(:remote => true)))

      return ctx unless block

      begin
        block.call(ctx)
      ensure
        ctx.close
      end
    end

    private

    def self.populate_properties(options = {})
      options = { :host => 'localhost',
                  :port => 4447,
                  :remote => false }.merge(options)

      properties = {}

      if options[:remote]
        properties[javax.naming.Context.INITIAL_CONTEXT_FACTORY] = REMOTE_NAMING_FACTORY
        properties[javax.naming.Context.PROVIDER_URL] = "remote://#{options[:host]}:#{options[:port]}"

        properties[javax.naming.Context.SECURITY_PRINCIPAL] = options[:username] if options[:username]
        properties[javax.naming.Context.SECURITY_CREDENTIALS] = options[:password] if options[:password]
      else
        properties[javax.naming.Context.INITIAL_CONTEXT_FACTORY] = LOCAL_NAMING_FACTORY
      end

      log.debug("Naming properties used to connect: #{properties}")

      java.util.Hashtable.new(properties)
    end

    def self.log
      @logger ||= TorqueBox::Logger.new(self)
    end
  end
end
