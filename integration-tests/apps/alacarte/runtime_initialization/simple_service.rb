
class SimpleService

  def initialize(opts={})
  end

  def start()
    begin
      ::ServiceHelper.new.write_message( "Hello from SimpleService!" )
    rescue
      FileUtils.rm_rf( ENV['TOUCHFILE'] )
    end
  end

  def stop()
    FileUtils.rm_rf( ENV['TOUCHFILE'] )
  end

end

