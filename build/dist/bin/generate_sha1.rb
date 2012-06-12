require 'digest/sha1'

Dir.glob( ARGV.first ) do |file|
  puts "Generating sha1 of: " + file
  data = File.open(file, 'r') { |f| f.read }
  File.open(file + '.sha1', 'w') { |f| f.write(Digest::SHA1.hexdigest(data)) }
end
