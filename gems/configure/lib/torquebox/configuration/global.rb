# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
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

require 'torquebox/configure'
require 'torquebox/configuration'

module TorqueBox
  module Configuration
    class GlobalConfiguration < TorqueBox::Configuration::Configuration
      def self.load_configuration(file)
        config = new
        TorqueBox::Configuration.load_configuration( file, config, ENTRY_MAP )
        config.to_metadata_hash
      end

      ENTRY_MAP = lambda do
        destination_entry =
          ThingWithOptionsEntry.with_settings(:validate => {
                                                :optional => [
                                                              { :create => [true, false] },
                                                              { :durable => [true, false] },
                                                              :processor,
                                                              :remote_host
                                                             ]
                                              })
        {
          :authentication => ThingWithOptionsEntry.with_settings( :validate => {
                                                                    :required => [:domain]
                                                                  }),
          :environment => OptionsEntry,
          :job         => ThingWithOptionsEntry.with_settings(:cumulative => true,
                                                              :validate => {
                                                                :required => [:cron],
                                                                :optional => [
                                                                              :config,
                                                                              :name,
                                                                              { :singleton => [true, false] }
                                                                             ]
                                                              }),
          :options_for => ThingWithOptionsEntry.with_settings(:validate => {
                                                                :optional => [
                                                                              :concurrency,
                                                                              { :disabled => [true, false] }
                                                                             ]
                                                              }),
          :pool        => ThingWithOptionsEntry.with_settings(:validate => {
                                                                :required => [{ :type => [:bounded, :shared] }],
                                                                :optional => [:min, :max]
                                                              }),
          :processor   => ThingWithOptionsEntry.with_settings(:require_parent => [:queue, :topic],
                                                              :cumulative => true,
                                                              :validate => {
                                                                :optional => [
                                                                              :concurrency,
                                                                              :config,
                                                                              :filter,
                                                                              :name
                                                                             ]
                                                              }),
          :queue       => destination_entry,
          :ruby        => OptionsEntry.with_settings(:validate => {
                                                       :optional => [{ :version => ['1.8', '1.9'] },
                                                                     { :compile_mode => [:force, :jit, :off,
                                                                                         'force', 'jit', 'off'] }]
                                                     }),
          :service     => ThingWithOptionsEntry.with_settings(:cumulative => true,
                                                              :validate => {
                                                                :optional => [
                                                                              :config,
                                                                              :name,
                                                                              { :singleton => [true, false] }
                                                                             ]
                                                              }),
          :topic       => destination_entry,
          :web         => OptionsEntry.with_settings(:validate => {
                                                       :optional => [:context, :host, :rackup, :static]
                                                     })
        }
      end.call


      def to_metadata_hash
        metadata = Hash.new { |hash, key| hash[key] = Hash.new { |hash, key| hash[key] = { } } }

        self[TorqueBox::CONFIGURATION_ROOT].each do |entry_name, entry_data|
          case entry_name
          when 'authentication' # => auth:
            entry_data.each do |name, data|
              metadata['auth'][name] = data
            end

          when 'job' # => jobs:
            entry_data.each do |klass, data|
              name = data.delete( :name ) || unique_name( klass.to_s, metadata['jobs'].keys )
              job = metadata['jobs'][name]
              job['job'] = klass.to_s
              job.merge!( data )
            end

          when 'options_for' # => tasks:
            entry_data.each do |name, data|
              data[:concurrency] = 0 if data.delete( :disabled )
              data[:concurrency] &&= data[:concurrency].to_java(java.lang.Integer)
              metadata['tasks'][name] = data
            end

          when 'pool' # => pooling:
            entry_data.each do |name, data|
              pool_type = data.delete( :type )
              if pool_type.to_s == 'shared'
                metadata['pooling'][name] = 'shared'
              else
                metadata['pooling'][name] = data
              end
            end

          when 'queue', 'topic' # => queues:/topics: & messaging:
            entry_data.each do |name, data|
              metadata[entry_name + 's'][name] = data unless data.delete( :create ) === false
              (data.delete( 'processor' ) || []).each do |processor|
                processor_class, processor_options = processor
                processor_options[:concurrency] &&= processor_options[:concurrency].to_java(java.lang.Integer)
                metadata['messaging'][name][processor_class] = processor_options
              end
            end

          when 'service' # => services:
            # TODO: use service name as the key once that's supported
            # elsewhere
            entry_data.each do |klass, data|
              # service = metadata['services'][name]
              # service['service'] = data.delete( :class ).to_s
              # service.merge!( data )
              metadata['services'][klass] = data
            end

          else # <entry_name>: (handles environment, ruby, web)
            metadata[entry_name] = entry_data
          end
        end

        hash_to_hashmap( metadata )
      end

      def hash_to_hashmap(hash)
        hashmap = java.util.HashMap.new
        hash.each do |key, value|
          value = hash_to_hashmap( value ) if value.is_a?( Hash )
          hashmap[key.to_s] = value
        end
        hashmap
      end

      protected
      def unique_name(name, set, suffix = nil)
        name = "#{name}-#{suffix}" if suffix
        name = unique_name( name, set, suffix.to_i + 1 ) if set.include?( name )
        name
      end
    end
  end
end

