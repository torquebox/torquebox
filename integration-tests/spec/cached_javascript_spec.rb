require 'spec_helper'
require 'torquebox/deploy_utils'

describe "cached javascript from an archive" do

  deploy { TorqueBox::DeployUtils.create_archive( :name => "torque-174.knob", 
                                                  :app_dir => File.join( File.dirname( __FILE__ ), "../apps/rails3/torque-174.knob" ),
                                                  :dest_dir => TorqueSpec.knob_root ) }

  it "should verify cached javascript works" do
    visit "/torque-174/top"
    page.find('#answer').should have_content( 'SUCCESS' )
  end

end
