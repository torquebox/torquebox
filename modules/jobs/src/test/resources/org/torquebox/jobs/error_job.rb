class ErrorJob

  def initialize(options={})
    @options = options
    @ran = false
    @timed_out = false
    @error = nil
  end

  def run
    @ran = true
    raise 'an error'
  end

  def on_error(error)
    @error = error.message
  end

  def ran?
    @ran
  end

  def error
    @error
  end
end
