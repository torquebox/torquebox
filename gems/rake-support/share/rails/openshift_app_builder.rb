
# The OpenShift AppBuilder automatically resolves any issues between 
class AppBuilder < Rails::AppBuilder

  def configru
    # get rid of the default config.ru from torquebox-openshift; Rails will replace it.
    remove_file 'config.ru'
    super
  end
  
  def readme
    # move the default Rails README to README.rails, since OSE provides one as well.
    copy_file 'README', 'README.rails'
  end
  
  def gitignore
    remove_file '.gitignore'
    super
    append_file '.gitignore', <<-DOC
java_to_ruby.rb
    DOC
  end

end