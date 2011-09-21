class Thing < ActiveRecord::Base

  attr_reader :callback
  attr_accessor :publish_callback
  alias_method :publish_callback?, :publish_callback

  %w{ commit rollback }.each do |event|
    %w{ before after }.each do |state|
      callback = "#{state}_#{event}"
      define_method(callback.to_sym) do 
        if publish_callback?
          TorqueBox::Messaging::Queue.new('/queue/output').publish(callback, :requires_new => true)
        end
        @callback = callback
      end
    end
  end

end
