
container_factory = Java::org.torquebox.enterprise.ruby.messaging.container.ContainerFactory.new

container_factory.connection_factory_jndi_name = "foo"


puts "container_factory=#{container_factory}"

container = container_factory.create_container

puts "container=#{container}"

