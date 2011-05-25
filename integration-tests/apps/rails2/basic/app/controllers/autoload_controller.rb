class AutoloadController < ApplicationController

  def index
    AppTask.new
    Task.new
    AppService.new
    Service.new
    AppJob.new
    Job.new
  end

end
