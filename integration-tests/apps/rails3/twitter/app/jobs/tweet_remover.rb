class TweetRemover
  include TorqueBox::Injectors
  
  def initialize
    @remover = fetch( Java::pl.goldmann.confitura.beans.TweetRemover )
  end
 
  def run
    @remover.remove(2)
  end

end
