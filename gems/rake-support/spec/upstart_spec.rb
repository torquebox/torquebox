require 'torquebox/upstart'

describe TorqueBox::Upstart do

  describe ".copy_init_script" do
    it "should not use the template" do
      pending "need fakefs or similar to properly test copy"
      # need fakefs or something
    end
  end

  describe ".process_init_template" do
    it "should substitute the values" do
      ENV["TORQUEBOX_HOME"] = File.expand_path("..", File.dirname(__FILE__))
      r = TorqueBox::Upstart.process_init_template("--server-config=standalone-ha.xml")
      r.should include("--server-config=standalone-ha.xml")
    end
  end
end
