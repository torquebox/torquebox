require 'torquebox/rails'

describe TorqueBox::Rails do

  context "Rails not installed" do
    before(:each) do
      TorqueBox::Rails.stub!(:rails_installed?).and_return(false)
    end

    describe "new_app" do
      it "should print a warning" do
        $stderr.should_receive(:puts)
        lambda {
          TorqueBox::Rails.new_app('root')
        }.should raise_error SystemExit
      end
    end

    describe "apply_template" do
      it "should print a warning" do
        $stderr.should_receive(:puts)
        lambda {
          TorqueBox::Rails.apply_template("root")
        }.should raise_error SystemExit
      end
    end

  end

  context "Rails 3" do
    before(:each) do
      TorqueBox::Rails.stub!(:rails_installed?).and_return(true)
      TorqueBox::Rails.stub(:using_rails3?).and_return(true)
      TorqueBox::Rails.stub(:require_generators)
      module ::Rails; module Generators; class AppGenerator; end; end; end
    end

    describe "new_app" do
      it "should generate" do
        ::Rails::Generators::AppGenerator.should_receive(:start)
        TorqueBox::Rails.new_app('root')
      end
    end

    describe "apply_template" do
      it "should apply" do
        generator = mock('generator')
        ::Rails::Generators::AppGenerator.stub(:new).and_return(generator)
        generator.should_receive(:apply).with(TorqueBox::Rails.template)
        TorqueBox::Rails.apply_template(File.expand_path("../fixtures/simpleapp", __FILE__))
      end
    end

  end

  context "Rails 2" do
    before(:each) do
      TorqueBox::Rails.stub!(:rails_installed?).and_return(true)
      TorqueBox::Rails.stub(:using_rails3?).and_return(false)
      TorqueBox::Rails.stub(:require_generators)
      module ::Rails; module Generator; class Base; end; end; end
      module ::Rails; module Generator; module Scripts; class Generate; end; end; end; end
      module ::Rails; class TemplateRunner; end; end
      ::Rails::Generator::Base.stub(:use_application_sources!)
    end

    describe "new_app" do
      it "should generate" do
        ::Rails::Generator::Scripts::Generate.any_instance.should_receive(:run)
        TorqueBox::Rails.new_app('root')
      end
    end

    describe "apply_template" do
      it "should apply" do
        ::Rails::TemplateRunner.should_receive(:new).with(TorqueBox::Rails.template)
        TorqueBox::Rails.apply_template('root')
      end
    end
  end

end
