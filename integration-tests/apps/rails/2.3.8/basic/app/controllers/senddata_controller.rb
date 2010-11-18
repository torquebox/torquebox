class SenddataController < ApplicationController

  def index
    send_data "this is the content"
  end

end
