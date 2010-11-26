class FormHandlingController < ApplicationController

  def index
    @value = params[:value]
  end

end
