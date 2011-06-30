#
# Copyright 2011 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
module Infinispan
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
      ::TrueClass                      => java.lang.Boolean,
    }


    def self.included(model)
      model.extend(ClassMethods)
      # XXX WARNING
      # <monkey-patching>
      # if class wasn't mapped before
      unless model.mapped?
        [:auto_migrate!, :auto_upgrade!, :create, :all, :copy, :first, :first_or_create, :first_or_new, :get, :last, :load].each do |method|
          model.before_class_method(method, :configure_index)
        end

        [:save, :update, :destroy, :update_attributes].each do |method|
          model.before(method) { model.configure_index }
        end
      end
      # </monkey-patching>
      
#      model.configure_index unless model.mapped?
    end

    module ClassMethods

      @@mapped = false

      def auto_migrate!(repo = nil)
        raise "Not implemented"
      end

      def auto_upgrade!(repo = nil)
        raise "Not implemented"
      end

      def to_java_type(type)
        TYPES[type] || self.to_java_type(type.primitive)
      end


      def to_java_class_name
        # http://jira.codehaus.org/browse/JRUBY-4601
        # return properly full-specified class name (ie rubyobj.Z.X.Y)
        "rubyobj."+self.to_s.gsub("::",".")
      end

      def mapped?
        @@mapped
      end

      def configure_index
        unless mapped?
          # just make sure all the properties are there
          # initialize join models and target keys
          relationships.each do |property, relationship|
            relationship.child_key
            relationship.parent_key
            relationship.through    if relationship.respond_to?(:through)
            relationship.via        if relationship.respond_to?(:via)
          end

          relationships().each do |rel|
            puts "---------------relationship: #{rel.inspect()}"
          end

          properties().each do |prop|
            puts "---------------property: #{prop.inspect()}"
            discriminator = add_java_property(prop) || discriminator
          end

          ## "stolen" from http://github.com/superchris/hibernate
          #annotation = {
            #javax.persistence.Entity => { },
            #javax.persistence.Table  => { "name" => self.storage_name }
          #}

          #if discriminator
            #annotation[javax.persistence.Inheritance]         = { "strategy" => javax.persistence.InheritanceType::SINGLE_TABLE.to_s }
            #annotation[javax.persistence.DiscriminatorColumn] = { "name" => discriminator }
          #end

          #add_class_annotation(annotation)

          #Hibernate.add_model(become_java!)

          #unless java.lang.Thread.currentThread.context_class_loader.is_a? JibernateClassLoader
            #cl = java.lang.Thread.currentThread.context_class_loader
            #if cl.is_a? org.jruby.util.JRubyClassLoader
              #java.lang.Thread.currentThread.context_class_loader = JibernateJRubyClassLoader.new(cl)
            #else
              #java.lang.Thread.currentThread.context_class_loader = JibernateClassLoader.new(cl)           
            #end
          #end

          #java.lang.Thread.currentThread.context_class_loader.register(java_class)

          #@@logger.debug "become_java! #{java_class}"
          @@mapped = true
        end
      end

      # "stolen" from http://github.com/superchris/hibernate
      def add_java_property(prop)
        #puts "#{prop.model.name} gets property added #{prop.name}"
        #name = prop.name
        #type = prop.class
        #return name if (type == DataMapper::Types::Discriminator)

        #column_name = prop.field
        #annotation = {}
        ## TODO honor prop.field mapping and maybe more
        #if prop.serial?
          #annotation[javax.persistence.Id] = {}
          #annotation[javax.persistence.GeneratedValue] = {}
        #elsif prop.key?
          ## TODO obey multi column keys
          #annotation[javax.persistence.Id] = {}
        #end

        #annotation[javax.persistence.Column] = {
                #"unique" => prop.unique?,
                #"name"   => prop.field
        #}

        #unless prop.index.nil?
          #if (prop.index == true)
            #annotation[org.hibernate.annotations.Index]
          #elsif (prop.index.class == Symbol)
            #annotation[org.hibernate.annotations.Index] = {"name" => prop.index.to_s}
          #else
            ## TODO arrays !!
            ##annotation[org.hibernate.annotations.Index] = {"name" => []}
            ##prop.index.each do|index|
            ##  annotation[org.hibernate.annotations.Index]["name"] << index.to_s
            ##end
          #end
        #end
        #if prop.required?
          #annotation[javax.persistence.Column]["nullable"] = !prop.required?
        #end
        #if (prop.respond_to?(:length) && !prop.length.nil?)
          #annotation[javax.persistence.Column]["length"] = java.lang.Integer.new(prop.length)
        #end
        #if (prop.respond_to?(:scale) && !prop.scale.nil?)
          #annotation[javax.persistence.Column]["scale"] = java.lang.Integer.new(prop.scale)
        #end
        #if (prop.respond_to?(:precision) && !prop.precision.nil?)
          #annotation[javax.persistence.Column]["precision"] = java.lang.Integer.new(prop.precision)
        #end

        #get_name = "get#{name.to_s.capitalize}"
        #set_name = "set#{name.to_s.capitalize}"

        ## TODO Time, Discriminator, EmbededValue
        ## to consider: in mu opinion those methods should set from/get to java objects...
        #if (type == DataMapper::Property::Date)
          #class_eval <<-EOT
            #def  #{set_name.intern} (d)
              #attribute_set(:#{name} , d.nil? ? nil : Date.civil(d.year + 1900, d.month + 1, d.date))
            #end
          #EOT
          #class_eval <<-EOT
            #def  #{get_name.intern}
              #d = attribute_get(:#{name} )
              #org.joda.time.DateTime.new(d.year, d.month, d.day, 0, 0, 0, 0).to_date if d
            #end
          #EOT
        #elsif (type == DataMapper::Property::DateTime)
          #class_eval <<-EOT
            #def  #{set_name.intern} (d)
              #attribute_set(:#{name} , d.nil? ? nil : DateTime.civil(d.year + 1900, d.month + 1, d.date, d.hours, d.minutes, d.seconds))
            #end
          #EOT
          #class_eval <<-EOT
            #def  #{get_name.intern}
              #d = attribute_get(:#{name} )
              #org.joda.time.DateTime.new(d.year, d.month, d.day, d.hour, d.min, d.sec, 0).to_date if d
            #end
          #EOT
        #elsif (type.to_s == BigDecimal || type == DataMapper::Property::Decimal)
          #class_eval <<-EOT
            #def  #{set_name.intern} (d)
              #attribute_set(:#{name} , d.nil? ? nil :#{type}.new(d.to_s))
            #end
          #EOT
          #class_eval <<-EOT
            #def  #{get_name.intern}
              #d = attribute_get(:#{name} )
              #java.math.BigDecimal.new(d.to_i) if d
            #end
          #EOT
        #else
          #class_eval <<-EOT
            #def  #{set_name.intern} (d)
              #attribute_set(:#{name} , d)
            #end
          #EOT
          #class_eval <<-EOT
            #def  #{get_name.intern}
              #d = attribute_get(:#{name} )
              #d
            #end
          #EOT
        #end

        #mapped_type = to_java_type(type).java_class
        #add_method_signature get_name, [mapped_type]
        #add_method_annotation get_name, annotation
        #add_method_signature set_name, [JVoid, mapped_type]
        #nil
      end
    end
  end
end
