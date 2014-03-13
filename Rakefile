require "#{File.dirname(__FILE__)}/tasks/torquebox"
TorqueBox::RakeHelper.install_standard_tasks
TorqueBox::RakeHelper.install_compile_tasks('wunderboss-torquebox',
                                            :gemspec => 'torqbox.gemspec',
                                            :copy_deps => 'lib/wunderboss-jars',
                                            :excluded_deps => ['jruby-complete'])

GEMS = %w(scheduling)

namespace :all do
  ['build', 'clean', 'install', 'release', 'spec'].each do |task_name|
    desc "Run #{task_name} for all modules"
    task task_name do
      errors = []
      system(%(#{$0} #{task_name})) || errors << 'torquebox'
      GEMS.each do |gem|
        system(%(cd #{gem} && #{$0} #{task_name})) || errors << gem
      end
      fail("Errors in #{errors.join(', ')}") unless errors.empty?
    end
  end
end


task :default => 'all:spec'
