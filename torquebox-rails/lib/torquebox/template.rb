
gem 'torquebox-gem'

gem "activerecord-jdbc-adapter",
    :lib=>'jdbc_adapter'

rakefile( 'torquebox.rake' ) do
  <<-TASK
    require 'torquebox/tasks'
  TASK
end
