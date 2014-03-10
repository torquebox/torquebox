module Rhino
  JAR_PATH = Dir.glob(
    File.expand_path('../rhino-*.jar', File.dirname(__FILE__))
  ).first
end