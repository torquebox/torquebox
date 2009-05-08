# JBoss, Home of Professional Open Source
# Copyright 2009, Red Hat Middleware LLC, and individual contributors
# by the @authors tag. See the copyright.txt in the distribution for a
# full listing of individual contributors.
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

import javax.servlet.sip.SipServlet

module JBoss 
  module Sip
    class SipBaseHandler < SipServlet  
	  protected
		def doBranchResponse(response)
		  self.do_branch_response(response)
		end  
		  
		def doErrorResponse(response)
		  self.do_error_response(response)
		end
		  
		def doRedirectResponse(response)
		  self.do_redirect_response(response)
		end  
		  
		def doProvisionalResponse(response)
		  self.do_provisional_response(response)
		end
		  
		def doSuccessResponse(response)
		  self.do_success_response(response)
		end
		  
		 
		def doAck(request)
		  self.do_ack(request)
		end		  		  
		  
		def doBye(request)
		  self.do_bye(request)
		end 
		  
		def doCancel(request)
		  self.do_cancel(request)
		end
		  
		def doInfo(request)
		  self.do_info(request)
		end
		  
		def doInvite(request)
		  self.do_invite(request)
		end   
		  
		def doMessage(request)
		  self.do_message(request)
		end
		  
		def doNotify(request)
		  self.do_notify(request)
		end  
		  
		def doOptions(request)
		  self.do_options(request)
		end
		  
		def doPrack(request)
		  self.do_prack(request)
		end
		  
		def doPublish(request)
		  self.do_publish(request)
		end
		  
		def doRefer(request)
		  self.do_refer(request)
		end
		  
		def doRegister(request)
		  self.do_register(request)
		end
		  
		def doSubscribe(request)
		  self.do_subscribe(request)
		end
		  
		def doUpdate(request)
		  self.do_update(request)
		end
		  
		def do_ack(request)		  	
		end		  		  
		  
		def do_bye(request)
		  if (request.initial?)
			not_handled(request)
	      end
		end 
		  
		def do_cancel(request)
		end
		  
		def do_info(request)
		  if (request.initial?)
		    not_handled(request)
		  end
		end
		  
		def do_invite(request)
		  if (request.initial?)
		    not_handled(request)
		  end
		end   
		  
		def do_message(request)
		  if (request.initial?)
			not_handled(request)
		  end
		end
		  
		def do_notify(request)
		  if (request.initial?)
		    not_handled(request)
		  end
		end  
		  
		def do_options(request)
		  if (request.initial?)
		    not_handled(request)
		  end
		end
		  
		def do_prack(request)
		  if (request.initial?)
			not_handled(request)
		  end
		end
		  
		def do_publish(request)
		  if (request.initial?)
			not_handled(request)
		  end
		end
		  
		def do_refer(request)
		  if (request.initial?)
		    not_handled(request)
		  end
		end
		  
		def do_register(request)
		  if (request.initial?)
			not_handled(request)
		  end
		end
		  
		def do_subscribe(request)
		  if (request.initial?)
			not_handled(request)
		  end
		end
		  
		def do_update(request)
		  if (request.initial?)
			not_handled(request)
		  end
		end   
		  
		def not_handled(request)
		  notHandled(request)
		end                	         
    end        
  end
end