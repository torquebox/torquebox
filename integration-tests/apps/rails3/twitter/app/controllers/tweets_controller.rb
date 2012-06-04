class TweetsController < ApplicationController
  include TorqueBox::Injectors

  # GET /tweets
  # GET /tweets.xml
  def index
    reader = fetch( Java::pl.goldmann.confitura.beans.TweetReader )
    
    @tweets = reader.read
    @total = reader.count

    respond_to do |format|
      format.html # index.html.erb
      format.xml  { render :xml => @tweets }
    end
  end
end
