# Copyright 2014 Red Hat, Inc, and individual contributors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

require 'spec_helper'

describe TorqueBox::CLI::ShellCommand do

  let(:command)       { 'echo "test"' }
  let(:shell_command) { TorqueBox::CLI::ShellCommand.new(command) }

  describe '#run' do
    it 'runs the command' do
      shell_command.run
      expect(shell_command.output.strip).to eq 'test'
    end

    it 'sets the exit code' do
      expect { shell_command.run }.to change { shell_command.exit_status }.from(nil).to(0)
    end

    it 'returns a reference to the shell command object' do
      expect(shell_command.run).to be_a TorqueBox::CLI::ShellCommand
    end

    context 'when the RUBYOPT environment variable is set' do
      let(:command) { 'echo $RUBYOPT' }

      before do
        @old_rubyopt = ENV['RUBYOPT']
        ENV['RUBYOPT'] = 'test'
      end

      after do
        ENV['RUBYOPT'] = @old_rubyopt
      end

      it 'clears the RUBYOPT environment variable' do
        shell_command.run
        expect(shell_command.output.strip).to be_empty
      end
    end

    context 'when the command throws an exception' do
      before do
        allow(shell_command).to receive(:run_command).and_raise('error')
      end

      it 'prints the error' do
        expect(shell_command).to receive(:print_error)
        shell_command.run
      end
    end
  end

  describe '#succeeded?' do
    context 'when the command has already run' do
      before do
        shell_command.run
      end

      it 'does not re-run the command' do
        expect(shell_command).to_not receive(:run)
        shell_command.succeeded?
      end
    end

    context 'when the command has not run yet' do
      it 'runs the command' do
        expect(shell_command).to receive(:run)
        shell_command.succeeded?
      end
    end

    context 'when the command succeeds' do
      let(:command) { 'true' }

      it 'returns true' do
        expect(shell_command.succeeded?).to be true
      end
    end

    context 'when the command fails' do
      let(:command) { 'false' }

      it 'returns false' do
        expect(shell_command.succeeded?).to be false
      end
    end
  end

  describe '#failed?' do
    context 'when the command succeeds' do
      let(:command) { 'true' }

      it 'returns false' do
        expect(shell_command.failed?).to be false
      end
    end

    context 'when the command fails' do
      let(:command) { 'false' }

      it 'returns true' do
        expect(shell_command.failed?).to be true
      end
    end
  end

  describe '#to_s' do
    it 'returns the output as a string' do
      shell_command.run
      expect(shell_command.to_s.strip).to eq 'test'
    end
  end
end
