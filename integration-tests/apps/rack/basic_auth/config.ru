require 'pp'

app = lambda { |env| 

  pp env

  if ( env['HTTP_AUTHORIZATION'].nil? ) 
    [
      401, { 'Content-Type' => 'text/html', 'WWW-Authenticate'=>'Basic realm="default"' }, 
      "must authenticate"
    ]
  else
    [
      200, { 'Content-Type' => 'text/html' }, 
      "<div><div id='env'>#{env.inspect.gsub(/</, '')}</div><div id='auth_header'>#{env['HTTP_AUTHORIZATION']}</div></div>"
    ] 
  end
}
run app
