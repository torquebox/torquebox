# Copyright 2008-2012 Red Hat, Inc, and individual contributors.
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

require 'dm-serializer'
require 'jruby/core_ext'
require 'json'


module Infinispan
  JVoid   = java.lang.Void::TYPE
  
  module Model

    # TODO enhance TYPEs list
    TYPES = {
      ::String                         => java.lang.String,
      ::Integer                        => java.lang.Integer,
      ::Float                          => java.lang.Double,
      ::BigDecimal                     => java.math.BigDecimal,
      ::Date                           => java.util.Date,
      ::DateTime                       => java.util.Date,
      ::Time                           => java.util.Date,
      ::TrueClass                      => java.lang.Boolean
    }

    def self.included(model)
      model.extend(ClassMethods)
      include java.io.Serializable

      unless model.mapped? model.name
        [:auto_migrate!, :auto_upgrade!, :create, :all, :copy, :first, :first_or_create, :first_or_new, :get, :last, :load].each do |method|
          model.before_class_method(method, :configure_index)
        end

        [:save, :update, :destroy, :update_attributes].each do |method|
          model.before(method) { model.configure_index }
        end
      end
    end

    def deserialize_to
      self.class.name
    end

    def is_a_with_hack?( thing )
      thing == java.lang.Object || is_a_without_hack?( thing )
    end

    alias_method :is_a_without_hack?, :is_a?
    alias_method :is_a?, :is_a_with_hack?

    module ClassMethods

      @@mapped = {}

      def auto_upgrade!
        configure_index
      end

      def auto_migrate!
        destroy
        configure_index
      end

      def to_java_type(type)
        TYPES[type] || self.to_java_type(type.primitive)
      end

      def mapped?( type )
        @@mapped[type]
      end

      def configure_index
        unless mapped?( name )
          configure_index!
        end
      end

      def configure_index!
        TorqueBox::Infinispan::Cache.log( "Configuring dm-infinispan-adapter model #{name}" )
        properties().each do |prop|
          TorqueBox::Infinispan::Cache.log( "Adding property #{prop.inspect}" )
          add_java_property(prop) 
          TorqueBox::Infinispan::Cache.log( "Added property #{prop.inspect}" )
        end

        annotation = {
          org.hibernate.search.annotations.Indexed => {},
          org.hibernate.search.annotations.ProvidedId => {},
          org.infinispan.marshall.SerializeWith => { "value" => org.torquebox.cache.marshalling.JsonExternalizer.java_class }
        }

        if JRUBY_VERSION =~ /^1\.7/
          add_class_annotations( annotation )
        else
          add_class_annotation( annotation )
        end

        # Wonder twin powers... ACTIVATE!
        java_class = become_java!(false)

        @@mapped[name] = true
      end

      def add_java_property(prop)
        name = prop.name
        type = prop.class

        column_name = prop.field
        annotation = {}

        annotation[org.hibernate.search.annotations.Field] = {}

        get_name = "get#{name.to_s.capitalize}"
        set_name = "set#{name.to_s.capitalize}"

        # TODO Time, Discriminator, EmbededValue
        # to consider: in mu opinion those methods should set from/get to java objects...
        if (type == DataMapper::Property::Date)
          class_eval <<-EOT
            def  #{set_name.intern} (d)
              attribute_set(:#{name} , d.nil? ? nil : Date.civil(d.year + 1900, d.month + 1, d.date))
            end
          EOT
          class_eval <<-EOT
            def  #{get_name.intern}
              d = attribute_get(:#{name} )
              java.util.Date.new( (Time.mktime(d.year, d.month, d.day).to_i * 1000) ) if d
            end
          EOT
        elsif (type == DataMapper::Property::DateTime)
          class_eval <<-EOT
            def  #{set_name.intern} (d)
              attribute_set(:#{name} , d.nil? ? nil : DateTime.civil(d.year + 1900, d.month + 1, d.date, d.hours, d.minutes, d.seconds))
            end
          EOT
          class_eval <<-EOT
            def  #{get_name.intern}
              d = attribute_get(:#{name} )
              java.util.Date.new( (Time.mktime(d.year, d.month, d.day, d.hour, d.min, d.sec, 0).to_i * 1000) ) if d
            end
          EOT
        elsif (type.to_s == BigDecimal || type == DataMapper::Property::Decimal)
          class_eval <<-EOT
            def  #{set_name.intern} (d)
              attribute_set(:#{name} , d.nil? ? nil :#{type}.new(d.to_s))
            end
          EOT
          class_eval <<-EOT
            def  #{get_name.intern}
              d = attribute_get(:#{name} )
              java.math.BigDecimal.new(d.to_i) if d
            end
          EOT
        else
          class_eval <<-EOT
            def  #{set_name.intern} (d)
              attribute_set(:#{name} , d)
            end
          EOT
          class_eval <<-EOT
            def  #{get_name.intern}
              d = attribute_get(:#{name} )
              d
            end
          EOT
        end

        mapped_type = to_java_type(type)
        add_method_signature get_name, [mapped_type]
        add_method_annotation get_name, annotation
        add_method_signature set_name, [JVoid, mapped_type]
      end
    end

  end
end
