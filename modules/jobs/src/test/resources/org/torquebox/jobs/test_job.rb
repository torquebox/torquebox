class TestJob

  def initialize(options={})
    @options = options
    @ran = false
  end

  def run
    @ran = true
    raise java.lang.IllegalStateException.new('an error') if @options['raise_error']
  end

  def ran?
    @ran
  end

  def [](key)
    @options[key]
  end

end
