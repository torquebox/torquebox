
app = lambda { |env| 
  [200, { 'Content-Type' => 'text/html' }, "<div id='success' class='basic-rack #{ENV['GRIST']}'>it worked</div><div id='ruby-version'>#{RUBY_VERSION}</div><div id='path'>#{__FILE__}</div><div id='context'>#{ENV['TORQUEBOX_CONTEXT']}</div><div id='path_info'>#{env['PATH_INFO']}</div><div id='request_uri'>#{env['REQUEST_URI']}</div>"] 
}
run app
