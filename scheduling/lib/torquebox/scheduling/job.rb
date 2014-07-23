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


module TorqueBox
  module Scheduling
    # Represents a scheduled job.
    #
    # (see {Scheduler#schedule})
    class Job

      # The id of this job
      attr_reader :id

      # Returns true if this job replaced an existing job with the
      # same id.
      def replacement?
        @replacement
      end

      # Unschedules this job.
      #
      # (see {Scheduler#unschedule})
      # @return true if a job was unscheduled, false otherwise
      def unschedule
        @scheduler.unschedule(@id)
      end

      protected
      def initialize(scheduler, id, replacement)
        @scheduler = scheduler
        @id = id
        @replacement = replacement
      end

    end
  end
end
