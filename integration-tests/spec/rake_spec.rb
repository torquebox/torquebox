require 'spec_helper'

# Run these remotely so our jruby_home, gem_home, etc are all setup
remote_describe "rake tasks" do

  it "should work" do
    output = rake('integ:sanity_check --trace')
    output.should include('sanity check passed')
  end

  def rake(task)
    command = "#{jruby_binary} -S rake -f #{File.dirname(__FILE__)}/../apps/rails3.1/basic/Rakefile #{task}"
    output = ""
    IO.popen4(command) do |pid, stdin, stdout, stderr|
      stdin.close
      stdout_reader = Thread.new(stdout) { |stdout_io|
        stdout_io.each_line { |l| output << l }
        stdout_io.close
      }
      stderr_reader = Thread.new(stderr) { |stderr_io|
        stderr_io.each_line { |l| output << l }
        stderr_io.close
      }
      [stdout_reader, stderr_reader].each(&:join)
      output
    end
  end

end

