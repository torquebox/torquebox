require 'open3'

def deployers_dir
  File.join( ENV['JBOSS_HOME'], 'server', ENV['JBOSS_CONF'] || 'default', 'deployers', 'torquebox.deployer' )
end

def execute_command(cmd)
  Open3.popen3( cmd ) do |stdin, stdout, stderr|
    stdin.close
    threads = []
    threads << Thread.new(stdout) do |str|
      while ( ! str.eof? ) 
        puts str.readline
      end
    end
    threads << Thread.new(stderr) do |str|
      while ( ! str.eof? ) 
        puts str.readline
      end
    end
    threads.each{|thr| thr.join}
  end
end

directory deployers_dir

task 'build-deployers' do
  Dir[ File.dirname(__FILE__) + '/*-int/' ].each do |deployer_module|
    Dir.chdir( deployer_module ) do
      execute_command( 'mvn package' )
    end
  end
end

task 'install-deployers'=>[ deployers_dir, 'install-integrations' ]

task 'prepare-deployers' do
  FileUtils.mkdir_p( deployers_dir )
end

task 'install-integrations' do
  Dir[ File.dirname(__FILE__) + '/*-int/target/*-deployer.jar' ].each do |deployer|
    base_name = File.basename( deployer )
    puts "Install integration #{base_name}"
    dest = File.join( deployers_dir, base_name )
    FileUtils.rm_f( dest )
    FileUtils.ln_s( deployer, dest )
  end
end

task 'install-gems' do
  puts "Install gems"
end

task 'install'=>['install-deployers']
task 'install'=>['install-gems']
