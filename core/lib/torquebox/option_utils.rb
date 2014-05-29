require 'set'

module TorqueBox
  module OptionUtils
    protected

    def validate_options(options, valid_keys)
      options.keys.each do |key|
        unless valid_keys.include?(key)
          fail ArgumentError.new("#{key} is not a valid option")
        end
      end
    end

    def opts_to_hash(opts_class)
      org.projectodd.wunderboss.Option.optsFor(opts_class).inject({}) do |hash, entry|
        hash[entry.name.to_sym] = entry
        hash
      end
    end

    def opts_to_set(opts_class)
      Set.new(opts_to_hash(opts_class).keys)
    end

    def optset(*things)
      set = Set.new
      things.each do |thing|
        if thing.kind_of?(Symbol)
          set << thing
        elsif thing.kind_of?(Enumerable)
          set += thing
        else
          set += opts_to_set(thing)
        end
      end
      set
    end

    def option_defaults(opts_class)
      org.projectodd.wunderboss.Option.optsFor(opts_class).inject({}) do |hash, entry|
        hash[entry.name.to_sym] = entry.defaultValue
        hash
      end
    end

    def extract_options(options, opts_class)
      opts_hash = opts_to_hash(opts_class)
      extracted_options = {}
      options.each_pair do |key, value|
        if opts_hash.include?(key)
          extracted_options[opts_hash[key]] = value
        end
      end
      extracted_options
    end
  end
end
