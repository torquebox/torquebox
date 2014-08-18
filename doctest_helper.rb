require 'torquebox/web'

TorqueBox::Logger.log_level = 'ERROR'

# Don't try to actually start servers by default
TorqueBox::Web::DEFAULT_SERVER_OPTIONS[:auto_start] = false

# Fake out a default rack app so config.ru files aren't looked for
TorqueBox::Web::DEFAULT_MOUNT_OPTIONS[:rack_app] = lambda { |env| [200, {}, []] }

YARD::Doctest.configure do |doctest|
  doctest.after do
    # Make sure if some example starts a server we shut things down
    Java::OrgProjectoddWunderboss::WunderBoss.shutdown_and_reset
  end
end
