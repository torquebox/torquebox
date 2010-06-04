require 'java'

$: << File.dirname(__FILE__) + '/../lib' 

require 'vfs'

TEST_DATA_BASE = 'target/test-data'
TEST_DATA_DIR = File.expand_path( File.join( File.dirname(__FILE__), "/../#{TEST_DATA_BASE}" ) )

