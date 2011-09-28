
class RootController < ApplicationController

  def index
  end

  def cachey
    Rails.cache.write( "taco", "crunchy" )
    @cache_value = Rails.cache.read( "taco" )
  end

  def cacheytx
    TorqueBox.transaction do
      Rails.cache.write( "taco", "crunchy" )
    end
    @cache_value = Rails.cache.read( "taco" )
    render "root/cachey"
  end

  def cacheytxthrows
    Rails.cache.write( "taco", "soft" )
    begin
      TorqueBox.transaction do
        Rails.cache.write( "taco", "crunchy" )
        raise "I like it soft"
      end
    rescue Exception => e
      # Exception should be "I like it soft"
    end
    @cache_value = Rails.cache.read( "taco" )
    render "root/cachey"
  end

end
