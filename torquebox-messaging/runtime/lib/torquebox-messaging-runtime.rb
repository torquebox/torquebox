
Dir[ File.dirname(__FILE__) + '/**/*.jar' ].each do |jar|
  require jar
end
