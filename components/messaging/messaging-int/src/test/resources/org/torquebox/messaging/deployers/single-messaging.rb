
TorqueBox::Messaging::Gateway.define do |gateway|
  gateway.subscribe 'MyClass', '/topics/foo', :filter=>'myfilter', :config=>{ :a=>"toast" }
end