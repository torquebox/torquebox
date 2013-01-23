require 'twitter4j4r'
require 'twitter-text'

class TwitterService
  # This will let us fetch the queue and topic later
  include TorqueBox::Injectors
  # We'll use it to extract the URLs from the tweet
  include Twitter::Extractor

  def initialize(credentials = {})
    @terms = []

    # Initialize logging
    @log = TorqueBox::Logger.new(TwitterService)

    @log.info "Initializing Twitter client..."

    # Create the Twitter client with credentials proviided in the
    # torquebox.yml file
    @client = Twitter4j4r::Client.new(
        :consumer_key     => credentials['consumer_key'],
        :consumer_secret  => credentials['consumer_secret'],
        :access_token     => credentials['access_token'],
        :access_secret    => credentials['access_secret']
    )

    @client.on_exception do |exception|
      @log.error "An error occured while reading the stream: #{exception.message}"
    end

    @log.info "Twitter client is ready"
  end

  def update(terms)
    @terms = terms

    stop
    start
  end

  def start
    if @terms.empty?
      @log.warn "No terms to watch. Twitter client will be not launched"
      return
    else
      @log.info "New terms arrived: #{@terms.join(', ')}"    
    end

    @log.info "Starting Twitter stream client..."

    @client.track(*@terms) do |status, client|
      urls = extract_urls(status.text)

      unless urls.empty?
        # Fetch the anchor to the queue
        queue = fetch('/queues/urls')

        # Publish the message (the URL in this case) to the queue
        urls.each do |url|
          queue.publish(url)
        end
      end
    end
  end

  def stop
    @log.info "Stopping Twitter client..."
    @client.stop
    @log.info "Client stopped"
  end
end

