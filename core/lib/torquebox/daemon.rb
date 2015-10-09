# Copyright 2015 Red Hat, Inc, and individual contributors.
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

require 'torquebox/option_utils'

module TorqueBox
  # Respresents a long-running daemon.
  #
  # When started, the daemon will execute its action in another
  # thread, and if that thread throws an unhandled exception, the
  # error will be logged and the action restarted.
  #
  # You can override this behavior by providing your own {#on_error}
  # handler.
  #
  # In a WildFly cluster, daemons will be singletons, meaning only one
  # instance of the daemon (based on its name) will run at a time.  If
  # the node running the daemon dies, it will start on another node as
  # long as {#start} has been called on a daemon with the same name
  # has been started on that node. If the daemon dies due to an error
  # and is restarted (via {#start}), then the current node will be in
  # competition with the other nodes in the cluster to start the
  # daemon, and it may actually start on another node.
  class Daemon
    include TorqueBox::OptionUtils
    extend TorqueBox::OptionUtils

    java_import org.projectodd.wunderboss::WunderBoss
    java_import org.projectodd.wunderboss.ec::DaemonContext

    DAEMON_OPTIONS = optset(DaemonContext::CreateOption,
                            :on_error,
                            :on_stop)

    # The name of this daemon.
    attr_reader :name

    # Creates a daemon object.
    #
    # Optionally takes a block that is the action of the daemon. If
    # not provided, you must extend this class and override {#action}
    # if you want the daemon to actually do anything. The block will
    # be passed the daemon object.
    #
    # @param name [String] The name of this daemon. Needs to be the
    #   same across a cluster for :singleton to work, and must be
    #   unique.
    # @param options [Hash] Options for the daemon.
    # @option options :singleton [true, false] (true)
    # @option options :on_error [Proc] A custom error handler, will be
    #   passed the daemon object and the error (see {#on_error})
    # @option options :on_stop [Proc] A custom stop handler, will be
    #   passed the daemon object (see {#on_stop})
    # @option options :stop_timeout [Number] (30_000) Milliseconds to
    #   wait for the daemon thread to exit when stopping.
    def initialize(name, options = {}, &block)
      validate_options(options, DAEMON_OPTIONS)
      @name = name.to_s
      @on_error_lambda = options.delete(:on_error)
      @on_stop_lambda = options.delete(:on_stop)
      @action_lambda = block
      @options = options

      if WunderBoss.find_component(DaemonContext.java_class, @name)
        raise "A daemon named #{@name} already exists"
      end

      @java_daemon =
        WunderBoss.find_or_create_component(DaemonContext.java_class,
                                            @name,
                                            extract_options(@options,
                                                            DaemonContext::CreateOption))
    end

    # Starts the daemon. If :singleton and in a cluster, the daemon
    # may not be running on the current node after calling start.
    # @return [Daemon] self
    def start
      @java_daemon.set_action do
        action
      end

      @java_daemon.set_error_callback do |_, err|
        on_error(err)
      end

      @java_daemon.set_stop_callback do |_|
        on_stop
      end

      @java_daemon.start

      self
    end

    # Stops the daemon. This will trigger the {#on_stop} callback if
    # this daemon is {#running?}.
    # @return [Daemon] self
    def stop
      if @java_daemon
        @java_daemon.stop
      end

      self
    end

    # true if the daemon is actually running. If not :singleton or
    # not in a cluster, and the action hasn't exited normally,
    # running? == {#started?}
    def running?
      @java_daemon && @java_daemon.is_running
    end

    # true if in the started state. May not actually be running. see
    # {#running?}
    def started?
      @java_daemon && @java_daemon.is_started
    end

    # The action to perform. If you override this method, the block
    # given to {#initialize} will be ignored. You should never call
    # this method directly.
    def action
      @action_lambda.call(self) if @action_lambda
    end

    # Called when an unhandled error occurs in #{action}. If you
    # override this method, the :on_error proc given to #{initialize}
    # will be ignored.
    #
    # @param error [] The error that occurred.
    def on_error(error)
      if @on_error_lambda
        @on_error_lambda.call(self, error)
      else
        DaemonContext::DEFAULT_ERROR_CALLBACK.notify(@java_daemon.name, error)
        start
      end
    end

    # Called when {#stop} is called, but only if {#started?} is true.
    def on_stop
      @on_stop_lambda.call(self) if @on_stop_lambda
    end
  end
end
