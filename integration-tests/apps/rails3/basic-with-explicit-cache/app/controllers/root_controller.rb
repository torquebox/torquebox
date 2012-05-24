
class RootController < ApplicationController

  def index
    @cache_type = Rails.cache.class.name
  end

end
