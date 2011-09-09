class Thing < ActiveRecord::Base

  %w{ validation save create commit rollback }.each do |event|
    %w{ before after }.each do |state|
      callback = "#{state}_#{event}"
      define_method(callback.to_sym) do 
        TorqueBox::Messaging::Queue.new('/queue/output').publish(callback, :new_session => true)
      end
    end
  end

end
