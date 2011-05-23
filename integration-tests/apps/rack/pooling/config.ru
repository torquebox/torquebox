
class App
  def call(env)
    puts "Invoking app"

    pool = TorqueBox::ServiceRegistry['jboss.deployment.unit."shared_runtime_pooling-knob.yml".torquebox.core.runtime.pool.web']
    pool ||= TorqueBox::ServiceRegistry['jboss.deployment.unit."bounded_runtime_pooling-knob.yml".torquebox.core.runtime.pool.web']

    min = max = ''
    if pool.respond_to?(:minimum_instances)
      min = pool.minimum_instances
      max = pool.maximum_instances
    end

    [200, { 'Content-Type' => 'text/html' }, %Q{it worked
<div id='pool-class'>#{pool.java_class.name}</div>
<div id='pool-min'>#{min}</div>
<div id='pool-max'>#{max}</div>
}] 
  end
end

run App.new
