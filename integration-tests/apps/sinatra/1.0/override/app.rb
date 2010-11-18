require 'sinatra'

get '/' do
  "<pre>\n" + ENV.keys.sort.map { |k| "#{k}=#{ENV[k]}" }.join("\n") + "</pre>"
end

get '/:k' do
  ENV[params[:k]]
end

