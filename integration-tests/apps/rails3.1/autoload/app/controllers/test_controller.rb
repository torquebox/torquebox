class TestController < ApplicationController
  def test
  	output = TestClass.test()
  	render :text => output
  end

end
