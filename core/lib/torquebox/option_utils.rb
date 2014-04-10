require 'set'

module TorqueBox
  module OptionUtils
    def validate_options(options, valid_keys)
      options.keys.each do |key|
        unless valid_keys.include?(key)
          raise ArgumentError.new("#{key} is not a valid option")
        end
      end
    end

    def enum_to_hash(enum)
      enum_values = enum.values.inject({}) do |hash, entry|
        hash[entry.value.to_sym] = entry
        hash
      end
    end

    def enum_to_set(enum)
      Set.new(enum_to_hash(enum).keys)
    end

    def extract_options(options, enum)
      enum_hash = enum_to_hash(enum)
      extracted_options = {}
      options.each_pair do |key, value|
        if enum_hash.include?(key)
          extracted_options[enum_hash[key]] = value
        end
      end
      extracted_options
    end
  end
end
