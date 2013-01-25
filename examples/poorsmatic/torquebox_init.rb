require 'data_mapper'
require 'models/term'
require 'models/url'

DataMapper::Logger.new(TorqueBox::Logger.new(DataMapper), :debug)
DataMapper.setup(:default, "sqlite:///#{File.dirname(__FILE__)}/poorsmatic.db")
DataMapper.auto_upgrade!

