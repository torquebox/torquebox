require 'data_mapper'
require 'models/term'
require 'models/url'

DataMapper::Logger.new(TorqueBox::Logger.new(DataMapper), :debug)
DataMapper.setup(:default, ENV['DB_URL'])
DataMapper.auto_upgrade!

