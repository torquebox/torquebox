require 'torquebox'
require 'sinatra'
require 'sinatra/reloader'
require 'haml'

require 'models/url'
require 'models/term'

class Poorsmatic < Sinatra::Base
  include TorqueBox::Injectors

  configure do
    use TorqueBox::Session::ServletStore
    use Rack::MethodOverride
  end

  configure :development do
    register Sinatra::Reloader
  end

  helpers do
    # This method is used to put the terms into the queue.
    # The method is executed every time a new term is added or deleted.
    def terms_changed
      terms = []

      Term.all.each {|t| terms << t.term}

      # Fetch the terms topic
      topic = fetch('/topics/terms')

      # Send the message (an array of terms) to the topic
      # even if this is an empty list
      topic.publish(terms)
    end
  end

  get '/' do
    haml :index
  end

  post '/terms' do

    term = Term.new(:term => params[:term])

    TorqueBox.transaction do
      if term.save
        terms_changed
      else
        session[:errors] = []
        term.errors.each {|e| session[:errors] << e.first }
      end
    end

    redirect to('/terms')
  end

  delete '/term/:id' do
    TorqueBox.transaction do
      Term.get(params[:id]).destroy
      terms_changed
    end

    redirect to('/terms')
  end
 
  get '/terms' do
    @terms = Term.all
    haml :terms
  end

  get '/urls' do
    @urls = Url.all
    haml :urls
  end
end
