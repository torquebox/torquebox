require "#{File.dirname(__FILE__)}/tasks/torquebox"
TorqueBox::RakeHelper.install_standard_tasks
TorqueBox::RakeHelper.install_compile_tasks('wunderboss-torquebox',
                                            :gemspec => 'torqbox.gemspec',
                                            :copy_deps => 'lib/wunderboss-jars',
                                            :excluded_deps => ['jruby-complete'])

MODULES = %w(torquebox-scheduling)

namespace :all do
  ['build', 'clean', 'install', 'release', 'spec'].each do |task_name|
    desc "Run #{task_name} for all modules"
    task task_name do
      errors = []
      system(%(#{$0} #{task_name})) || errors << 'torquebox'
      MODULES.each do |mod|
        system(%(cd #{mod} && #{$0} #{task_name})) || errors << mod
      end
      fail("Errors in #{errors.join(', ')}") unless errors.empty?
    end
  end
end


task :default => 'all:spec'
