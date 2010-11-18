require 'sinatra'

get '/:k' do
  ENV[params[:k]] || all
end

def all
  "<pre>\n" + ENV.keys.sort.map { |k| "#{k}=#{ENV[k]}" }.join("\n") + "</pre>"
end
