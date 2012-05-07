#!/usr/bin/env ruby

ENV['GEM_HOME'] = ENV['GEM_HOME'].gsub('gems/1.8', 'gems/shared') if JRUBY_VERSION =~ /^1\.7/

require File.dirname(__FILE__) + '/../lib/gem_installer.rb'

versions = {
  :jruby_openssl  =>    '0.7.4',
  :haml           =>    '3.1.3',
  :json           =>    '1.6.1',
  :rails2x        =>    '2.3.14',
  :rails30        =>    '3.0.11',
  :rails31        =>    '3.1.3',
  :rails32        =>    '3.2.2',
  :sass_rails     =>    {
    :rails31 => '3.1.5',
    :rails32 => '3.2.3',
  },
  :coffee_rails   =>    {
    :rails31 => '3.1.1',
    :rails32 => '3.2.1',
  },
  :therubyrhino   =>    '1.72.8',
  :uglifier       =>    '1.0.3',

  :jquery_rails   =>    '1.0.14',

  :arjdbc11       =>    '1.1.3',
  :arjdbc12       =>    '1.2.0',

  :jdbc_h2        =>    '1.3.154',
  :jdbc_sqlite3   =>    '3.7.2',

  :sinatra        =>    '1.2.3',
  :sinatra_flash  =>    '0.3.0',

  :rack11         =>    '1.1.2',
  :rack12         =>    '1.2.4',
  :rack13         =>    '1.3.3',

  :dm_core        =>    '1.1.0',
  :json_for_dm    =>    '1.4.6',

  :padrino        =>    '0.10.5',
  :bcrypt_ruby    =>    '3.0.1',

  :sequel         =>    '3.29.0'
}

#GemInstaller.into( File.dirname(__FILE__) + '/../target/integ-dist/jruby/lib/ruby/gems/1.8', versions ) do |installer|

GemInstaller.with( versions ) do |installer|
  installer.install( 'jruby-openssl' )
  installer.install( 'haml'          )
  installer.install( 'json' )
  installer.install( 'rails',         versions[:rails2x] )
  installer.install( 'rails',         versions[:rails30] )
  installer.install( 'rails',         versions[:rails31] )
  installer.install( 'rails',         versions[:rails32] )
  installer.install( 'therubyrhino' )
  installer.install( 'sass-rails',    versions[:sass_rails][:rails31] )
  installer.install( 'coffee-rails',  versions[:coffee_rails][:rails31] )
  installer.install( 'sass-rails',    versions[:sass_rails][:rails32] )
  installer.install( 'coffee-rails',  versions[:coffee_rails][:rails32] )
  installer.install( 'jquery-rails',  versions[:jquery_rails], false )
  installer.install( 'uglifier' )
  
  installer.install( 'activerecord-jdbc-adapter', versions[:arjdbc11] )
  installer.install( 'activerecord-jdbc-adapter', versions[:arjdbc12] )

  installer.install( 'jdbc-h2'      )
  installer.install( 'jdbc-sqlite3' )
  
  installer.install( 'sinatra' )

  installer.install( 'rack', versions[:rack11] )
  installer.install( 'rack', versions[:rack12] )
  installer.install( 'rack', versions[:rack13] )
  
  installer.install( 'data_mapper',       versions[:dm_core] )
  installer.install( 'dm-sqlite-adapter', versions[:dm_core] )
  installer.install( 'dm-serializer', versions[:dm_core] )

  installer.install( 'json', versions[:json_for_dm] )
  
  installer.install( 'padrino', versions[:padrino] )
  installer.install( 'bcrypt-ruby' )
  installer.install( 'sinatra-flash', versions[:sinatra_flash] )

  installer.install( 'sequel', versions[:sequel] )
end
