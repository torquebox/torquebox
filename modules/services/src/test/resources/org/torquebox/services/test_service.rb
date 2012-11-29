class TestService

  attr_reader :start_count

  def initialize options={}
    @options = options
    @started = false
    @start_count = 0
  end

  def start
    @started = true
    @start_count += 1
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
