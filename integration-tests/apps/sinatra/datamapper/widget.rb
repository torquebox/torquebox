require 'dm-core'
require 'datamapper/dm-infinispan-adapter'


DataMapper.setup(:default, :adapter=>'infinispan', :persist=>false)
