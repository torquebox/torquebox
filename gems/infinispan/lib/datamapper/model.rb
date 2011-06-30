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
      model.configure_index unless model.mapped?
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
          #discriminator = add_java_property(prop) || discriminator
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
  end
end
