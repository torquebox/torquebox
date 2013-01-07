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
  module Infinispan

    class Sequence
      include java.io.Serializable

      class Codec
        def self.encode(sequence)
          sequence.value.to_s
        end

        def self.decode(sequence_bytes)
          sequence_bytes && Sequence.new( sequence_bytes.to_s.to_i )
        end
      end

      def initialize(amount = 1) 
        @data = amount
      end

      def value
        @data ? @data.to_i : @data
      end

      def next(amount = 1)
        Sequence.new( @data.to_i + amount )
      end

      def ==(other)
        self.value == other.value
      end

      def to_s
        "Sequence: #{self.value}"
      end
    end

  end
end


