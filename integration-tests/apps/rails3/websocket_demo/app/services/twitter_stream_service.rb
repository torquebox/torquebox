
class TwitterStreamService

  include TorqueBox::Injectors
  
  attr :tweets_topic
  
  def initialize(options = {})
    @username = options['username']
    @password = options['password']
    
    @random_users = %w(user1 user2 user3 user4 user5 user6)
    @random_tweets = [ 
      "Just found #torquebox - it's great!",
      "Hey #torquebox - do you support sinatra apps?",
      "Using #torquebox to integrate with backend services - works great thus far.",
      "Just deployed #torquebox Backstage for administration stuffs.",
      "Wow. It is ridiculously easy to get started with @torquebox, even with RVM.",
      "This one goes to 11. #torquebox",
      "rebuilding our jruby application servers for this project with #torquebox. such a wonderful fit for an enterprisy as this project is.",
      "Pięknie! Moja prezentacja o #TorqueBox została zaakceptowana na @confiturapl! Do zobaczenia w Warszawie, 11.06.",
      "Just submitted a #torquebox talk to @strangeloop_stl. Wish me luck!",
      "Hadn't come across TorqueBox before - anybody checked it out? http://t.co/Bdck2Hy",
      "RT @mojavelinux: Create a linux system service for #JBoss AS in about 5 minutes using foreman: http://goo.gl/xHcra Thanks #torquebox guys for the tip!",
      "We penguins love #torquebox.",
      "#java TorqueBox: An Arbitrary Update: TorqueBoxAs the cold snap ends, the peony buds begin to open, and I sta... http://bit.ly/ixvAzh"
    ]
    @random_lat_longs = [
      [38.92,-77.23],     # Washington, DC. 
      [35.23,-80.84],     # Charlotte, NC
      [37.53,-77.44],     # Richmond, VA
      [41.88,-87.64],     # Chicago, IL
      [-33.84,151.21],    # Sydney, NSW, Australia
      [45.58,-122.58],    # Portland, OR
      [43.9116,-79.4467], # Toronto, CA
      [51.52,-0.16],      # London, UK
      [32.91,-96.93],     # Dallas, TX,
      [-54.4333, 3.4],    # Bouvet Island, NOR!
      [-37.066667, -12.316667] # Tristan de Cunha, UK
    ]
    
  end
  
  def start
    @tweets_topic = TorqueBox::Messaging::Topic.new '/topics/tweets' 
    Thread.new {  monitor }
  end
  
  def stop
  end
  
  private
  
  def monitor
    puts "username is #{@username} and password is #{@password}."
    puts "twitter stream service :: the tweet topic is #{@tweets_topic}."
    counter = 0
    while true
      puts "Starting the loop."
      lat_long = @random_lat_longs[rand(11)]
      message = {:type => :tweet, :tweet => {
        :user_id => @random_users[rand(6)], 
        :id => counter,
        :text => @random_tweets[rand(13)], 
        :latitude => lat_long[0], 
        :longitude => lat_long[1], 
        :time => Time.now.strftime('%m/%d/%Y %H:%M')
      }}
      puts "publishing new data: #{message.inspect}."
      @tweets_topic.publish(message.to_json)
      sleep(30)
      counter += 1
    end
=begin    
    t = TwitterStream.new :username => @username, :password => @password
    t.track('java') do |status|
      user = status['user']
      tweet = Tweet.new :user_id => user['screen_name'], :text => status['text']
      user = status['user']
      puts "the new status - #{status}"
    end
=end
  end
  
end