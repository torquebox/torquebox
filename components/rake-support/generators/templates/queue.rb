require 'torquebox/queues/base'

<% module_names.each_with_index do |module_name, index| -%>
<%= "  " * index %>module <%= module_name %><%= "\n" -%>
<% end %>
<% prefix = "  " * module_names.size -%>
<%= prefix -%>class <%= the_class_name %>
<%= prefix -%>  include TorqueBox::Queues::Base

<%= prefix -%>  def task_action( payload = {} )
<%= prefix -%>    log.info "#{self.class}.task_action"
<%= prefix -%>  end

<%= prefix -%>end
end