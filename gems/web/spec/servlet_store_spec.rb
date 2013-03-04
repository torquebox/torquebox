require 'torquebox/session/servlet_store'


describe TorqueBox::Session::SessionData do

	subject { TorqueBox::Session::SessionData.new }

	it "stores symbols as strings" do
		subject[:a] = "hello"
		subject["a"].should == "hello"
	end

	it "stores string as strings" do
		subject["a"] = "hello"
		subject["a"].should == "hello"
	end

	it "retrives strings using symbols" do
		subject["a"] = "hello"
		subject[:a].should == "hello"
	end

	it "works with symbols even though they are stored as strings" do
		subject[:a] = "hello"
		subject[:a].should == "hello"
	end

end


