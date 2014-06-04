source :rubygems

gemspec :name => "therubyrhino"

group :test do
  # NOTE: some specs might be excluded @see #spec/spec_helper.rb
  gem 'redjs', :git => 'git://github.com/cowboyd/redjs.git', :group => :test,
               :ref => "0d844f066666f967a78b20beb164c52d9ac3f5ca"
  #gem 'redjs', :path => '../redjs', :group => :test
  
  # e.g. `export therubyrhino_jar=1.7.3`
  if jar_version = ENV['therubyrhino_jar']
    gem 'therubyrhino_jar', jar_version
  else
    gem 'therubyrhino_jar', :path => '.'
  end
  gem 'less', '>= 2.2.1', :require => nil
end