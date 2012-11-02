require 'digest/sha1'

Dir.glob(ARGV.first) do |file|
  puts "Generating sha1 of: " + file
  sha1 = Digest::SHA1.new
  File.open(file, 'r') do |f|
    until f.eof?
      sha1.update(f.read(16384))
    end
  end
  File.open(file + '.sha1', 'w') { |f| f.write(sha1.hexdigest) }
end
