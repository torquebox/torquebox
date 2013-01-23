require 'rest-client'
require 'nokogiri'
require 'models/url'

class UrlScrapper < TorqueBox::Messaging::MessageProcessor

  def initialize
    # Initialize logging
    @log = TorqueBox::Logger.new(UrlScrapper)
  end

  def on_message(url)
    unless Url.first(:url => url).nil?
      @log.info "The specified url '#{url}' is already in database, skipping"
      return
    end

    # OK, the url is not found in database, let's fetch it
    if raw = retrieve(url)

      page = Nokogiri::HTML(raw)
      body = page.xpath("/html/body").text
      title = page.xpath("/html/head/title").text
      count = body.scan(/[\w]+/).size

      @log.debug "Document parsed, got #{count} words"

      begin
        Url.new(:url => url, :title => title.strip, :count => count).save!
      rescue Exception => e
        # For example Facebook does some Javascript thingy to redirect, skip these
        @log.warn "Couldn't save link for url #{url}; #{e.message}"
      end
    end
  end

  def retrieve(url)
    @log.debug "Fetching #{url}..."

    begin
      return RestClient::Request.execute(:method => :get, :url => url, :timeout => 5, :open_timeout => 5)
    rescue Exception => e
      @log.warn "Cannot get #{url}; #{e.message}"
    end
  end
end
