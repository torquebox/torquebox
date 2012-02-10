require 'torquebox/messaging/datamapper_marshaling'

class MyTestDataMapperModel
  include TorqueBox::Messaging::DataMapper
  def id ; 100 end
end

describe TorqueBox::Messaging::DataMapper do

  describe "DataMapper::Resource" do
    it "should _dump as a string with the ID and class name" do
      model = MyTestDataMapperModel.new
      id, clazz = model._dump(-1).split(':')
      clazz.should == model.class.name
      id.should == "100"
    end

    it "should call Resource.get with the id on Marshal.load" do
      MyTestDataMapperModel.should_receive(:get).with("100")
      Marshal.load(Marshal.dump(MyTestDataMapperModel.new))
    end
  end

end

