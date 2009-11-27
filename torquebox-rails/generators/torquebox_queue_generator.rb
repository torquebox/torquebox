# Created by Dan Tylenda-Emmons
# Twitter: jrubyist
# Email: jrubyist-at-gmail-dot-com
# Since: 11/27/2009

class TorqueboxQueueGenerator < Rails::Generator::NamedBase
  
  def manifest
    raise "Class name must end with Queue" unless the_class_name =~ /.*Queue\z/

    record do |m|
      m.directory "app/queues"
      m.directory "app/queues/#{module_names_path}"
      m.template "queue.rb", "app/queues/#{module_names_path}/#{file_name}.rb"
    end    
  end
  
  def module_names_path
    module_names.join('/').downcase
  end

  def the_class_name
    class_name_parts.last
  end

  def module_names
    class_name_parts - [the_class_name]
  end
  
  def class_name_parts
    class_name.split(/::/)
  end
  
end
