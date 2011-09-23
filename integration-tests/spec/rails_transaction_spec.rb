require 'spec_helper'

shared_examples_for "transactions" do

  require 'torquebox-messaging'

  before(:each) do
    @input  = TorqueBox::Messaging::Queue.new('/queue/input')
    @output  = TorqueBox::Messaging::Queue.new('/queue/output')
    Thing.delete_all
    Person.delete_all
  end
    
  it "should save a simple thing" do
    sally = Thing.create(:name => 'sally')
    sally.callback.should == 'after_commit'
    Thing.find_by_name("sally").name.should == 'sally'
  end

  it "should create a Thing in response to a happy message" do
    @input.publish("happy path")
    @output.receive(:timeout => 60_000).should == 'after_commit'
    sleep 2
    Thing.count.should == 1
    Thing.find_by_name("happy path").name.should == "happy path"
  end

  it "should not create a Thing in response to an error prone message" do
    @input.publish("this will error")
    msgs = []
    loop do
      msg = @output.receive(:timeout => 30_000)
      raise "Didn't receive enough rollback messages" unless msg
      msgs << msg if msg == 'after_rollback'
      break if msgs.size == 10  # default number of HornetQ delivery attempts
    end
    Thing.count.should == 0
    Thing.find_all_by_name("this will error").should be_empty
  end

  it "should continue to have surprising behavior with nested non-TorqueBox transactions" do
    Thing.transaction do
      Thing.create(:name => 'bob')
      Thing.transaction do
        Thing.create(:name => 'ben')
        raise ActiveRecord::Rollback
      end
    end
    Thing.find_by_name('bob').name.should == 'bob'
    Thing.find_by_name('ben').name.should == 'ben'
    Thing.count.should == 2
  end

  it "should have less surprising behavior wrapped in a TorqueBox transaction" do
    TorqueBox.transaction do
      Thing.transaction do
        Thing.create(:name => 'bob')
        Thing.transaction do
          Thing.create(:name => 'ben')
          raise ActiveRecord::Rollback
        end
      end
    end
    Thing.find_by_name('bob').should be_nil
    Thing.find_by_name('ben').should be_nil
    Thing.count.should == 0
  end

  it "should support :requires_new for creating models in TorqueBox transactions" do
    TorqueBox.transaction do
      Thing.create(:name => 'Kotori')
      TorqueBox.transaction(:requires_new => true) do
        Thing.create(:name => 'Nemu')
        raise ActiveRecord::Rollback
      end
    end
    Thing.find_by_name('Kotori').should_not be_nil
    Thing.find_by_name('Nemu').should be_nil
  end

  it "should support :requires_new for updating models in TorqueBox transactions" do
    sally = Thing.create(:name => 'sally')
    ethel = Thing.create(:name => 'ethel')
    sally.callback.should == 'after_commit'
    ethel.callback.should == 'after_commit'
    sally.name = 'fred'
    ethel.name = 'barney'
    TorqueBox.transaction do
      sally.save!
      TorqueBox.transaction(:requires_new => true) do
        ethel.save!
        raise ActiveRecord::Rollback
      end
    end
    sally.callback.should == 'after_commit'
    ethel.callback.should == 'after_rollback'
    Thing.find_all_by_name("fred").size.should == 1
    Thing.find_all_by_name("barney").should be_empty
    Thing.find_all_by_name("sally").should be_empty
    Thing.find_all_by_name("ethel").size.should == 1
  end

  it "should save to multiple class-specific databases in a TorqueBox transacation" do
    TorqueBox.transaction do
      Thing.create(:name => 'sue')
      Person.create(:name => 'sue', :age => 42)
    end
    Thing.find_by_name('sue').should_not be_nil
    Person.find_by_name('sue').should_not be_nil
  end

  it "should rollback from multiple class-specific databases in a TorqueBox transacation" do
    TorqueBox.transaction do
      Thing.create(:name => 'sue')
      Person.create(:name => 'sue', :age => 42)
      raise ActiveRecord::Rollback
    end
    Thing.find_by_name('sue').should be_nil
    Person.find_by_name('sue').should be_nil
  end

  it "should rollback as expected for a non-XA connection" do
    test_rollback ActiveRecord::Base.method(:transaction)
  end

  it "should rollback as expected for an XA connection" do
    test_rollback TorqueBox.method(:transaction)
  end

  it "should rollback correctly when nesting a non-TorqueBox tx in a TorqueBox one" do
    test_nested_rollback ActiveRecord::Base.method(:transaction)
  end

  it "should rollback correctly when nesting two TorqueBox transactions" do
    test_nested_rollback TorqueBox.method(:transaction)
  end

  def test_rollback meth
    sally = Thing.create(:name => 'sally')
    ethel = Thing.create(:name => 'ethel')
    sally.callback.should == 'after_commit'
    ethel.callback.should == 'after_commit'
    sally.name = 'fred'
    ethel.name = 'barney'
    meth.call do
      sally.save!
      ethel.save!
      raise ActiveRecord::Rollback
    end
    sally.callback.should == 'after_rollback'
    ethel.callback.should == 'after_rollback'
    Thing.find_all_by_name("fred").should be_empty
    Thing.find_all_by_name("barney").should be_empty
    Thing.find_all_by_name("sally").size.should == 1
    Thing.find_all_by_name("ethel").size.should == 1
  end

  def test_nested_rollback meth
    TorqueBox.transaction do
      Thing.create(:name => 'bob')
      meth.call do
        Thing.create(:name => 'ben')
        raise ActiveRecord::Rollback
      end
    end
    Thing.find_by_name('bob').should be_nil
    Thing.find_by_name('ben').should be_nil
    Thing.count.should == 0
  end

end

remote_describe "rails 3.0 transactions testing" do

  deploy <<-END.gsub(/^ {4}/,'')
    ---
    application:
      root: #{File.dirname(__FILE__)}/../apps/rails3/transactions
      env: development
    ruby:
      version: #{RUBY_VERSION[0,3]}
  END

  it_should_behave_like "transactions"
end

# remote_describe "rails 3.1 transactions testing" do

#   deploy <<-END.gsub(/^ {4}/,'')
#     ---
#     application:
#       root: #{File.dirname(__FILE__)}/../apps/rails3.1/transactions
#       env: development
#     ruby:
#       version: #{RUBY_VERSION[0,3]}
#   END

#   it_should_behave_like "transactions"
# end
