
app = lambda { |env| [200, { 'Content-Type' => 'text/html' }, '<div id="success" class="basic-rack">it worked</div>'] }
run app
