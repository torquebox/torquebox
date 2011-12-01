namespace :integ do
  desc "Ensure Rake can load the Rails Environment"
  task :sanity_check => :environment do
    puts "sanity check passed"
  end
end
