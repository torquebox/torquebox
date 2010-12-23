class OptionalComponent
  def initialize(options)
    @options = options
  end
  def [] k
    @options[k]
  end
end
