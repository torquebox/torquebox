class TestService

  def initialize options={}
    @options = options
    @started = false
  end

  def start
    @started = true
  end

  def stop
    @started = false
  end

  def started?
    @started
  end

  def [] key
    @options[key]
  end

  def options_class_name
    @options.class.name
  end

end
