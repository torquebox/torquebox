task :default => 'spec'

GEMS = %w(core messaging scheduling web caching)

%W(build clean install release spec).each do |task_name|
  desc "Run #{task_name} for all gems"
  task task_name do
    errors = []
    GEMS.each do |gem|
      puts ">>> Running #{task_name} for #{gem} gem"
      ENV['JAVA_OPTS'] = '-XX:+TieredCompilation -XX:TieredStopAtLevel=1'
      success = system(%(cd #{gem} && #{$PROGRAM_NAME} #{task_name}))
      unless success
        errors << gem
        break
      end
      puts ""
    end
    fail("Errors in #{errors.join(', ')}") unless errors.empty?
  end
end

require "#{File.dirname(__FILE__)}/tasks/torquebox"
TorqueBox::RakeHelper.install_bundler_tasks
TorqueBox::RakeHelper.install_clean_tasks

task 'build' do
  all_jars = GEMS.map do |gem|
    Dir.glob("#{gem}/lib/**/*.jar")
  end.flatten
  jar_names = all_jars.map { |jar| File.basename(jar) }.sort
  unique_jar_names = jar_names.uniq
  unique_jar_names.each do |jar|
    jar_names.delete_at(jar_names.find_index(jar))
  end
  unless jar_names.empty?
    puts "ERROR: Duplicate jars found: #{jar_names.inspect}"
    exit 1
  end

  all_gems = (GEMS + ['.']).map do |gem|
    Dir.glob("#{gem}/pkg/*.gem")
  end.flatten
  puts "Gem sizes:"
  total_size_kb = 0
  all_gems.each do |gem|
    size_kb = File.size(gem) / 1024
    total_size_kb += size_kb
    puts "  #{File.basename(gem)}: #{size_kb} KB"
  end
  puts "  Total: #{total_size_kb} KB"
  max_size_kb = 10752
  if total_size_kb > max_size_kb
    puts "ERROR: Maximum combined gem size of #{max_size_kb} KB exceeded"
    exit 1
  end
end

desc 'Run an irb session with all torquebox libraries on the load path'
task 'irb' do
  GEMS.each do |gem|
    $LOAD_PATH << "#{gem}/lib"
  end
  require 'irb'
  ARGV.clear
  IRB.start
end

desc 'Generate documentation via yardoc'
task 'doc' do
  require 'fileutils'
  require 'yard'
  require 'yaml'

  version_path = File.join(File.dirname(__FILE__), 'core', 'lib', 'torquebox', 'version.rb')
  require version_path

  files_to_clean = []

  FileUtils.mkdir_p('pkg')
  readme = File.read('README.md')
  readme.sub!(/^# TorqueBox/, "# TorqueBox #{TorqueBox::VERSION}")
  File.open('pkg/README.md', 'w') { |f| f.write(readme) }
  files_to_clean << 'pkg/README.md'

  guides = YAML.load_file('docs/guides.yml')
  final_guides = guides.map do |f|
    content = File.read("docs/#{f}.md")
    guides.each do |guide|
      content.gsub!("(#{guide}.md)", "(file.#{guide}.html)")
    end
    out_file = "pkg/#{f}.md"
    File.open(out_file, 'w') { |out| out.write(content) }
    files_to_clean << out_file
    out_file
  end

  YARD::CLI::Yardoc.new.run('--title', "TorqueBox #{TorqueBox::VERSION}",
                            '-r', 'pkg/README.md', '-', *final_guides)
  files_to_clean.map { |f| FileUtils.rm_f(f) }
end

# Run yard-doctest to test all our @example tags
namespace 'doc' do
  desc 'Run specs for all doc @example blocks'
  task 'spec' do
    jruby_command = File.join(RbConfig::CONFIG['bindir'], RbConfig::CONFIG['ruby_install_name'])
    success = system(%(#{jruby_command} -r 'bundler/setup' -S yard doctest '*/lib/**/*.rb'))
    fail unless success
  end
end
task 'doc:spec' => 'build'
task 'spec' => 'doc:spec'

require 'rubocop/rake_task'
RuboCop::RakeTask.new(:rubocop) do |cop|
  cop.options = %W(-D)
end

# purposely no description so it's hidden from rake -T
task 'update_version' do
  new_version = ENV['VERSION']
  unless new_version
    $stderr.puts "Error: You must specify the new version via $VERSION"
    exit 1
  end
  version_path = File.join(File.dirname(__FILE__), 'core', 'lib', 'torquebox', 'version.rb')
  require version_path
  current_version = TorqueBox::VERSION
  contents = File.read(version_path)
  contents.sub!(current_version, new_version)
  File.open(version_path, 'w') { |f| f.write(contents) }
end


require "#{File.dirname(__FILE__)}/tasks/incremental"
TorqueBox::IncrementalTasks.install
