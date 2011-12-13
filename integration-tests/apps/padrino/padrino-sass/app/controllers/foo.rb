PadrinoSass.controllers :foo do

  get :index, :map => "/foo" do
    render 'foo/foo'
  end

  
end
