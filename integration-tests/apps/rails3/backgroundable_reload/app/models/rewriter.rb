class Rewriter
  class << self
    def rewrite_file(file_name, replace, replacement)
      lines = File.readlines( file_name )
      File.open( file_name, 'w' ) do |f|
        lines.each do |line|
          f.write( line.gsub( replace, replacement ) )
        end
      end
    end
  end
end
