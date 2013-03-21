class TweetRemover

  def initialize
    @remover = TorqueBox.fetch( Java::pl.goldmann.confitura.beans.TweetRemover )
  end

  def run
    @remover.remove(2)
  end

end
