
require 'highline'
require 'open3'

ROOT_REPOSITORY=File.dirname( __FILE__ )

task 'init' do
  run( "git submodule update --init" )  
  each_repository( :up ) do
    run "git checkout master"
  end
end

task 'add-commit-remotes' do
  each_repository do |repo|
   run( 'git remote show origin -n' ) do |stdout|
      lines = stdout.readlines
      url_line = lines[1].strip
      remote_name, remote_url = url_line.split
      remote_url.strip!
      github_regexp = %r(^git(@|://)github\.com(:|/)(torquebox/torquebox.*))
      if ( remote_url =~ github_regexp )
        `git remote show commit 2>&1 > /dev/null`
        if ( $? != 0 )
          tail = $3
          if ( remote_url =~ /git@github.com/ )
            run( "git remote add commit #{remote_url}", true, true )
          else
            pushable_url = "git@github.com:#{tail}"
            run( "git remote add commit #{pushable_url}", true, true )
          end
        else
          puts "remote 'commit' already exists, skipping"
        end
      end
    end
  end
end

task 'remove-tag' do
  input = HighLine.new
  tag_name = input.ask( "Remove what tag?" ).strip
  puts "removing #{tag_name}"
  each_repository do
    run "git tag -d #{tag_name}", true, true
    run "git push commit :#{tag_name}"
  end
end

task 'commit-all' do
  script = []
  each_repository do
    script << "pushd #{Dir.pwd} && git commit -a && popd"
  end
  puts script.join( " && \\\n " )
end

def run(cmd,actually_run=true, ignore_exit=false, &block)
  cmd.strip!
  if ( $last_announced_dir != Dir.pwd )
    $last_announced_dir = Dir.pwd
  end
  puts "execute [#{cmd}]"
  if ( ! actually_run )
    return
  end
  Open3.popen3( cmd ) do |stdin, stdout, stderr|
    stdout_thread = Thread.new {
      if ( block )
        block.call( stdout )
      else
        while ( ! stdout.eof? )
          puts stdout.readline
        end
      end
    }
    stderr_thread = Thread.new {
      while ( !stderr.eof? )
        puts stderr.readline
      end
    }
    stdin.close()
    stdout_thread.join
    stderr_thread.join
  end
  return if ignore_exit
  raise "Command failure, see output" if $? != 0
end


def each_repository(mode=:down,path=ROOT_REPOSITORY,&block)
  Dir.chdir( path ) do
    if ( mode == :down )
      if ( File.exist?( '.git' ) )
        puts "(in #{Dir.pwd})"
        block.call
      end
    end
    Dir['*'].each do |entry|
      if ( File.directory?( entry ) && entry != 'target')
        each_repository( mode, entry, &block )
      end
    end
    if ( mode == :up )
      if ( File.exist?( '.git' ) )
        block.call
      end
    end
  end
end
