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

require %q(org/torquebox/web/rails/v2_3/rackup_generator)
generator = TorqueBox::Rails::V2_3::Rack::Generator

module TorqueBox
  module Rails
    def self.app=(app)
      @app = app
    end

    def self.app
      @app
    end
  end
end
TorqueBox::Rails.app = generator.generate( TORQUEBOX_RACKUP_CONTEXT )
