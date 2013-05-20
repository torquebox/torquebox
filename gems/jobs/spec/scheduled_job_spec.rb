require 'torquebox/jobs/scheduled_job'

describe TorqueBox::Jobs::ScheduledJob do
  context "'at' jobs" do
    it "should raise if no options are specified" do
      lambda {
        TorqueBox::Jobs::ScheduledJob.at('Class', nil)
      }.should raise_error("Invalid options for scheduling the job")
    end

    it "should raise if invalid options are specified" do
      lambda {
        TorqueBox::Jobs::ScheduledJob.at('Class', "something")
      }.should raise_error("Invalid options for scheduling the job")
    end

    it "should raise if :at and :in options are both specified" do
      lambda {
        TorqueBox::Jobs::ScheduledJob.at('Class', :at => Time.now, :in => 2_000)
      }.should raise_error("You can't specify both :at and :in")
    end

    it "should raise if :repeat is used without :every" do
      lambda {
        TorqueBox::Jobs::ScheduledJob.at('Class', :at => Time.now, :repeat => 2_000)
      }.should raise_error("You can't specify :repeat without :every")
    end

    it "should raise if :until is used without :every" do
      lambda {
        TorqueBox::Jobs::ScheduledJob.at('Class', :at => Time.now, :until => Time.now + 2)
      }.should raise_error("You can't specify :until without :every")
    end

    it "should raise if the :in parameter is not an Fixnum" do
      lambda {
        TorqueBox::Jobs::ScheduledJob.at('Class', :in => Time.now)
      }.should raise_error("Invalid type for :in, should be a Fixnum")

    end
  end

  context "scheduled job" do
    it "should raise if the job class is not provided" do
      lambda {
        TorqueBox::Jobs::ScheduledJob.schedule(nil, '')
      }.should raise_error("No job class name provided")
    end

    it "should raise if the cron expression is not provided" do
      lambda {
        TorqueBox::Jobs::ScheduledJob.schedule('Class', nil)
      }.should raise_error("No cron expression provided")
    end
  end
end

