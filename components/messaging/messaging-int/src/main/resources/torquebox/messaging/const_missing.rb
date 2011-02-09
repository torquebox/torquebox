
unless defined?(ActiveSupport)
  puts "WARN: Defining const_missing"
  def Object.const_missing(name)
    file = org.torquebox.common.util.StringUtils.underscore(name)
    require file
    result = const_get(name)
    return result if result
    raise "Class not found: #{name}"
  end
end
