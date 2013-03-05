
class App
  def call(env)
    [200, { 'Content-Type' => 'text/html' }, "it worked"]
  end
end

run App.new
