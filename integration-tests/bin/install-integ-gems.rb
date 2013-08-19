#!/usr/bin/env ruby

SLIM_DISTRO = java.lang::System.getProperty( "org.torquebox.slim_distro" ) == "true"
def jruby_home
  if SLIM_DISTRO
    File.expand_path( File.join( File.dirname( __FILE__ ), '..', 'target', 'integ-dist', 'jruby' ) )
  else
    File.join( Dir.glob( File.join( File.dirname( __FILE__ ), '..', 'target', 'integ-dist', 'jboss-eap-6*' ) ).first, 'jruby' )
  end
end

if JRUBY_VERSION =~ /^1\.7/
  ENV['GEM_HOME'] = "#{jruby_home}/lib/ruby/gems/shared"
else
  ENV['GEM_HOME'] = "#{jruby_home}/lib/ruby/gems/1.8"
end
ENV['GEM_PATH'] = ENV['GEM_HOME']

require File.dirname(__FILE__) + '/../lib/gem_installer.rb'

versions = {
  :jruby_openssl  =>    '0.7.7',
  :haml           =>    '3.1.6',
  :json           =>    '1.7.4',
  :rails2x        =>    '2.3.14',
  :rails30        =>    '3.0.15',
  :rails31        =>    '3.1.7',
  :rails32        =>    '3.2.7',
  :rails40        =>    '4.0.0',
  :sass_rails     =>    {
    :rails31 => '3.1.6',
    :rails32 => '3.2.5',
    :rails40 => '4.0.0',
  },
  :coffee_rails   =>    {
    :rails31 => '3.1.1',
    :rails32 => '3.2.2',
    :rails40 => '4.0.0',
  },
  :therubyrhino   =>    '1.73.5',
  :uglifier       =>    {
    :rails3x => '1.0.4',
    :rails40 => '2.1.1',
  },
  :jquery_rails   =>    {
    :rails3x => '1.0.19',
    :rails40 => '3.0.2',
  },
  :turbolinks     =>    '1.2.0',
  :jbuilder       =>    '1.4.2',
  :sdoc           =>    '0.3.20',

  :arjdbc11       =>    '1.1.3',
  :arjdbc12       =>    '1.2.9.1',

  :jdbc_h2        =>    '1.3.170.1',
  :jdbc_sqlite3   =>    '3.7.2.1',

  :sinatra        =>    '1.2.8',
  :sinatra_flash  =>    '0.3.0',

  :rack11         =>    '1.1.3',
  :rack12         =>    '1.2.5',
  :rack13         =>    '1.3.6',

  :dm_core        =>    '1.1.0',
  :json_for_dm    =>    '1.4.6',

  :padrino        =>    '0.10.5',
  :bcrypt_ruby    =>    '3.0.1',

  :sequel         =>    '3.37.0',
  :thor           =>    '0.16.0',

  :newrelic_rpm   =>    '3.3.2'
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
  installer.install( 'rails',         versions[:rails40] )
  installer.install( 'therubyrhino' )
  installer.install( 'sass-rails',    versions[:sass_rails][:rails31] )
  installer.install( 'coffee-rails',  versions[:coffee_rails][:rails31] )
  installer.install( 'sass-rails',    versions[:sass_rails][:rails32] )
  installer.install( 'coffee-rails',  versions[:coffee_rails][:rails32] )
  installer.install( 'sass-rails',    versions[:sass_rails][:rails40] )
  installer.install( 'coffee-rails',  versions[:coffee_rails][:rails40] )
  installer.install( 'jquery-rails',  versions[:jquery_rails][:rails3x], false )
  installer.install( 'jquery-rails',  versions[:jquery_rails][:rails40], false )
  installer.install( 'uglifier',      versions[:uglifier][:rails3x] )
  installer.install( 'uglifier',      versions[:uglifier][:rails40] )
  installer.install( 'turbolinks',    versions[:turbolinks] )
  installer.install( 'jbuilder',      versions[:jbuilder] )
  installer.install( 'sdoc',          versions[:sdoc] )

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
  installer.install( 'thor', versions[:thor] )

  installer.install( 'newrelic_rpm', versions[:newrelic_rpm] )
end
