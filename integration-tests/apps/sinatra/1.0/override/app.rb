
get '/' do
  "#{APP}:<pre>\n" + ENV.keys.sort.map { |k| "#{k}=#{ENV[k]}" }.join("\n") + "</pre>"
end

get '/:k' do
  k = params[:k]
  "#{APP}: #{k}=#{ENV[k]}"
end

