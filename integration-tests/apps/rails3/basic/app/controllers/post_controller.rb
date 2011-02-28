class PostController < ApplicationController

  def raw
    if ( request.post? )
      @raw_post = request.raw_post
      @name = params[:name]
    end
  end
end
