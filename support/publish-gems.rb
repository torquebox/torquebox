#!/usr/bin/env ruby

gems_we_have = Dir['*.gem']

puts "Usage: cd to dir where you unzipped the gem download, and run me there." if gems_we_have == []

gem_map = gems_we_have.inject({ }) do |accum, gem|
  gem =~ /(.*?)-\d.*/
  accum[$1] = gem
  accum
end

gem_order = %w{
    torquebox-core
    torquebox-capistrano-support
    torquebox-rake-support
    torquebox-configure
    torquebox-jobs
    torquebox-naming
    torquebox-security
    torquebox-services
    torquebox-transactions
    torquebox-cache
    torquebox-messaging
    torquebox-stomp
    torquebox-web
    torquebox
    torquebox-server
    torquebox-no-op
}

if gem_map.keys.sort != gem_order.sort
  puts "The gems in this dir don't match the expected gems. Expected: #{gem_order.sort.inspect}, actual: #{gems_we_have.inspect}"
  exit
end

gem_order.each do |gem|
  gem_file = gem_map[gem]
  puts "Publishing #{gem_file}"
  system( %Q{ gem push #{gem_file} } )
end

puts "DONE"
