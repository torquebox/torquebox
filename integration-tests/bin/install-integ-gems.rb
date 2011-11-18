#!/usr/bin/env ruby

require File.dirname(__FILE__) + '/../lib/gem_installer.rb'

versions = {
  :jruby_openssl  =>    '0.7.4',
  :haml           =>    '3.1.3',
  :rails2x        =>    '2.3.14',
  :rails30        =>    '3.0.10',
  :rails31        =>    '3.1.0',
  :rails31        =>    '3.1.0',
  :rails312       =>    '3.1.2rc2',
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
  :json           =>    '1.4.6',

  :padrino        =>    '0.10.4',

  :sequel         =>    '3.29.0'
}

#GemInstaller.into( File.dirname(__FILE__) + '/../target/integ-dist/jruby/lib/ruby/gems/1.8', versions ) do |installer|

GemInstaller.with( versions ) do |installer|
  installer.install( 'jruby-openssl' )
  installer.install( 'haml'          )
  installer.install( 'rails',         versions[:rails2x] )
  installer.install( 'rails',         versions[:rails30] )
  installer.install( 'rails',         versions[:rails31] )
  installer.install( 'rails',         versions[:rails312] )
  installer.install( 'therubyrhino' )
  installer.install( 'sass-rails',    versions[:rails31] )
  installer.install( 'coffee-rails',  versions[:rails31] )
  installer.install( 'jquery-rails' )
  installer.install( 'uglifier' )
  
  installer.install( 'activerecord-jdbc-adapter', versions[:arjdbc11] )
  installer.install( 'activerecord-jdbc-adapter', versions[:arjdbc12] )

  installer.install( 'jdbc-h2'      )
  installer.install( 'jdbc-sqlite3' )
  
  installer.install( 'sinatra' )

  installer.install( 'rack', versions[:rack11] )
  installer.install( 'rack', versions[:rack12] )
  installer.install( 'rack', versions[:rack13] )
  
  installer.install( 'dm-core',       versions[:dm_core] )
  installer.install( 'dm-serializer', versions[:dm_core] )

  installer.install( 'json' )
  
  installer.install( 'padrino', versions[:padrino] )
  installer.install( 'sinatra-flash', versions[:sinatra_flash] )

  installer.install( 'sequel', versions[:sequel] )
end
