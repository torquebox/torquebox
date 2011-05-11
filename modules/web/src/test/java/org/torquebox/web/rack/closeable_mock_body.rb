
class CloseableMockBody < Array
  
  def initialize()
    @closed = false 
  end
  
  def close()
    @closed = true 
  end
  
  def closed?()
    return @closed    
  end
  
end