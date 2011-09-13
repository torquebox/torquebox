class Thing < ActiveRecord::Base

  attr_reader :callback

  %w{ commit rollback }.each do |event|
    %w{ before after }.each do |state|
      callback = "#{state}_#{event}"
      define_method(callback.to_sym) do 
        TorqueBox::Messaging::Queue.new('/queue/output').publish(callback, :new_session => true)
        @callback = callback
      end
    end
  end

end
