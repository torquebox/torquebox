require 'spec_helper'
require 'fileutils'

BASEDIR    = File.join( File.dirname(__FILE__).gsub( %r(\\:), ':' ).gsub( %r(\\\\), '\\' ), '..', 'target' )
TOUCHFILE  = File.join( BASEDIR, 'alacarte-runtime-touchfile.txt' )

describe "alacarte runtime initialization test" do

  deploy <<-END.gsub(/^ {4}/,'')

    application:
      root: #{File.dirname(__FILE__)}/../apps/alacarte/runtime_initialization
      env: development
    
    environment:
      TOUCHFILE: #{TOUCHFILE}
    
    ruby:
      version: #{RUBY_VERSION[0,3]}

  END

  it "should work" do
    touchfile = Pathname.new( TOUCHFILE )
    touchfile.should exist
  end

end

