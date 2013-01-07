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

module TorqueBox
  module Messaging
    module ProcessorMiddleware
      class Chain

        def prepend(klass, *args)
          chain.unshift(MWare.new(klass, args)) unless locate(klass)
          self
        end

        def append(klass, *args)
          chain << MWare.new(klass, args) unless locate(klass)
          self
        end

        alias_method :add, :append

        def remove(klass)
          loc = locate(klass)
          chain.delete_at(loc) if loc
          self
        end

        def inspect
          chain.map(&:klass).inspect
        end
        
        def invoke(session, message, processor)
          realized_chain = realize
          walker = lambda do
            mware = realized_chain.shift
            if mware
              mware.call(session, message, &walker)
            else
              processor.process!(message)
            end
          end
          walker.call
        end

        protected

        def chain
          @chain ||= []
        end

        def locate(klass)
          chain.index { |m| m.klass == klass }
        end

        def realize
          chain.map(&:instance)
        end
      end

      class MWare
        attr_reader :klass

        def initialize(klass, args)
          @klass = klass
          @args = args
        end

        def instance
          @klass.new(*@args)
        end
      end
      
    end
  end
end
