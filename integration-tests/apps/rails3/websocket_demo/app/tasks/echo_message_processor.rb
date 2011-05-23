
class EchoMessageProcessor < TorqueBox::Messaging::WebSocketsProcessor

  include TorqueBox::Injectors
  
  def initialize(params = {})
    @tasks_queue = TorqueBox::Messaging::Topic.new '/topics/workflow' 
  end
  
  def on_message(message)
    begin
      puts "Processing request #{message}."
      request = JSON.parse(message)
      Tweet.transaction do
      
        to = Tweet.create
        tweet = request['tweet']
        tweet.each { |k, v| to.send(:"#{k}=", v) if to.has_attribute?(k) }
        to.tweet_time = Date.parse(tweet['time'])
        to.save!

        username = request['user']
        user = User.find_by_username username
      
        task = TweetTask.create :user => user, :tweet => to, :state => TweetTask::ASSIGNED
        task.save!
        
        message = {:type => :task, :task => task}
        json = message.to_json(:include => {:tweet => {:except => [:created_at, :updated_at] }, :user => {:only => :username }})
        puts "The resulting JSON is #{json}."
        @tasks_queue.publish json
      end
      
    rescue => e
      on_error e
    end
    puts "Done processing message."
  end
  
  def on_error(error)
    puts "Encountered error #{error}."
    puts "Encountered error #{error.backtrace}."
  end
  
end
