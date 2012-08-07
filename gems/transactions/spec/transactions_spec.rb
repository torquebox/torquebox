require 'torquebox/transactions'

describe TorqueBox::Transactions do

  describe "argument parsing" do

    before(:each) do
      @mgr = TorqueBox::Transactions::Manager.new
    end

    it "should parse resources followed by a symbol" do
      resources, method = @mgr.parse_args(1, 2, 3, :requires_new)
      resources.should == [1, 2, 3]
      method.should == :requires_new
    end

    it "should parse only a symbol" do
      resources, method = @mgr.parse_args(:requires_new)
      resources.should == []
      method.should == :requires_new
    end

    it "should parse no args, returning :required, by default" do
      resources, method = @mgr.parse_args
      resources.should == []
      method.should == :required
    end

    it "should parse :none to :not_supported" do
      resources, method = @mgr.parse_args(:none)
      resources.should == []
      method.should == :not_supported
    end

    it "should parse resources followed by a hash" do
      resources, method = @mgr.parse_args(1, 2, 3, :requires_new => true)
      resources.should == [1, 2, 3]
      method.should == :requires_new
    end

    it "should parse only a hash" do
      resources, method = @mgr.parse_args(:requires_new => true)
      resources.should == []
      method.should == :requires_new
    end

    it "should parse only a hash containing :scope key" do
      resources, method = @mgr.parse_args(:scope => :mandatory)
      resources.should == []
      method.should == :mandatory
    end

    it "should parse only a hash, preferring :scope to :requires_new" do
      resources, method = @mgr.parse_args(:scope => :required, :requires_new => true)
      resources.should == []
      method.should == :required
    end

  end
end
