
class RootController < ApplicationController

  def index
  end


  def hamltest
  end

  def injectiontest
    puts "About to call thing_one()"
    @use_me = thing_one()
    puts "Called thing_one() -> #{@use_me} #{@use_me.class} #{@use_me.java_class.name}"
  end

  def databaseyml
    cfg = Rails.application.config
    puts ">>>>>>>>>>>>>>>>>> Got config: #{cfg.to_s}"
    db_cfg = cfg.database_configuration
    puts ">>>>>>>>>>>>>>>>>> Got database_config: #{db_cfg.to_s}"
    @db_user = Rails.application.config.database_configuration["production"]["username"]
    puts "LOOKING FOR USER"
    puts ">>>>>>>>>>>>>>>>>> Got database user: #{db_cfg["production"]["username"]}"
  end

  def environment
  end

end
