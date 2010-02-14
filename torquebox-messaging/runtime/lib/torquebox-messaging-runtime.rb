
require 'java'
if ( ! defined?( Java::javax.jms.Connection ) )
  Dir[ File.dirname(__FILE__) + '/**/*.jar' ].each do |jar|
    require jar
  end
end
