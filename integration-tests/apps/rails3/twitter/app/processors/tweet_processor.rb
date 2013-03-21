class TweetProcessor < TorqueBox::Messaging::MessageProcessor

  def initialize
    @tweet_saver = TorqueBox.fetch( Java::pl.goldmann.confitura.beans.TweetSaver )
  end

  def on_message(tweet)
   @tweet_saver.save(tweet[:username], tweet[:message], tweet[:id])
  end
end
