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

begin
  javax.jms::Session
rescue
  # $stderr.puts "Will not load torquebox-messaging: javax.jms.* cannot be loaded"
  return 
end

require 'torquebox/messaging/connection_factory'
require 'torquebox/messaging/connection'
require 'torquebox/messaging/session'

require 'torquebox/messaging/message'
require 'torquebox/messaging/json_message'
require 'torquebox/messaging/edn_message'
require 'torquebox/messaging/text_message'
require 'torquebox/messaging/marshal_base64_message'
require 'torquebox/messaging/marshal_message'

require 'torquebox/messaging/destination'
require 'torquebox/messaging/queue'
require 'torquebox/messaging/topic'

require 'torquebox/messaging/xa_connection_factory'
require 'torquebox/messaging/xa_connection'
require 'torquebox/messaging/xa_session'

require 'torquebox/messaging/processor_middleware/chain'
require 'torquebox/messaging/processor_middleware/with_transaction'
require 'torquebox/messaging/message_processor'
require 'torquebox/messaging/task'
require 'torquebox/messaging/backgroundable'


require 'torquebox/messaging/datamapper_marshaling'
