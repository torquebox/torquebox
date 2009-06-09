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

require 'org/torquebox/rails/web/v2_2/servlet_rack_request'

module JBoss
  module Rails
    module Rack
      class Dispatcher < ActionController::Dispatcher
        def initialize(relative_url_root)
          @relative_url_root = relative_url_root
        end
        def call(env)
          ActionController::Base.relative_url_root = @relative_url_root
          @request  = JBoss::Rails::Rack::ServletRackRequest.new(env)
          @response = ActionController::RackResponse.new(@request)
          dispatch
        end
      end
    end
  end
end