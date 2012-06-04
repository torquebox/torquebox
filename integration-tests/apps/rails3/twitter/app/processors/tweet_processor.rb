class TweetProcessor < TorqueBox::Messaging::MessageProcessor
  include TorqueBox::Injectors

  def initialize
    @tweet_saver = fetch( Java::pl.goldmann.confitura.beans.TweetSaver )
  end

  def on_message(tweet)
   @tweet_saver.save(tweet[:username], tweet[:message], tweet[:id])
  end
end
