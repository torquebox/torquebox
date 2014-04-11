GEMS = %w(core scheduling web)

['build', 'clean', 'install', 'release', 'spec'].each do |task_name|
  desc "Run #{task_name} for all gems"
  task task_name do
    errors = []
    GEMS.each do |gem|
      puts ">>> Running #{task_name} for #{gem} gem"
      ENV['JAVA_OPTS'] = '-XX:+TieredCompilation -XX:TieredStopAtLevel=1'
      success = system(%(cd #{gem} && #{$0} #{task_name}))
      unless success
        errors << gem
        break
      end
      puts ""
    end
    fail("Errors in #{errors.join(', ')}") unless errors.empty?
  end
end

require "#{File.dirname(__FILE__)}/etc/tasks/torquebox"
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
  max_size_kb = 10240
  if total_size_kb > max_size_kb
    puts "ERROR: Maximum combined gem size of #{max_size_kb} KB exceeded"
    exit 1
  end
end

task 'irb' do
  GEMS.each do |gem|
    $: << "#{gem}/lib"
  end

  File.open("/tmp/tbirbrc", "w") do |f|
    f.write("IRB.conf[:AT_EXIT] << lambda {require 'torquebox-core';org.projectodd.wunderboss.WunderBoss.stop}\n")
  end
  ENV["IRBRC"] = "/tmp/tbirbrc"
  require 'irb'
  ARGV.clear
  IRB.start
end

task :default => 'spec'
