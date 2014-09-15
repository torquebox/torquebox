# @title Scheduling Guide

# Scheduling Asynchronous Jobs

TorqueBox provides support for registering jobs to execute
asynchronously on a schedule.

The API for scheduling all resides within the
[Scheduling module](TorqueBox/Scheduling.html), though most of your
interaction will be with one method:
[Scheduler.schedule](TorqueBox/Scheduling/Scheduler.html#schedule-class_method).

## The gem

Scheduled jobs are provided by the `torquebox-scheduling` gem, and can
be used independently of other TorqueBox services.

## Scheduling Jobs

A job is basically a zero-arity block that will be executed based on a
schedule you provide. To schedule a job, provide an id, a schedule,
and your block to `Scheduler.schedule`:

    TorqueBox::Scheduling::Scheduler.schedule(:job1, every: 1000) do
      puts "I fire every second"
    end

Jobs can be scheduled with a delayed start, to execute a limited
number of times or up until a stop time, or scheduled based on a
cron-like schedule. For more details on the available schedule
options, see the
[Scheduler.schedule docs](TorqueBox/Scheduling/Scheduler.html#schedule-class_method).

Jobs can be scheduled at runtime, from anywhere within an application.

### Using ActiveSupport extensions

If you are using
[ActiveSupport](https://rubygems.org/gems/activesupport), you can use
its numeric extensions with any of the options that take a time
period:

    TorqueBox::Scheduling::Scheduler.schedule("my-job", in: 5.minutes, every: 1.second) do
      puts "I fire every second, starting in 5 minutes"
    end

### In-container behavior

When used inside a [WildFly](wildfly.md) cluster, jobs are
highly-available singletons by default. This means that if a job with
the same id is scheduled on multiple nodes, it will only initially run
on the first node where it fires. If that node goes down, one of the
other nodes where that same job is scheduled will automatically take
over.

If you want to schedule a job with the same id on all nodes that does
actually need to run on every node, you can disable the singleton
behavior as part of the schedule for the job:

    TorqueBox::Scheduling::Scheduler.schedule("every-node", every: 1000, singleton: false) do
      puts "I fire on every node where I'm scheduled"
    end

## Unscheduling Jobs

To unschedule a previously scheduled job, you have two options:

1) call [Scheduler.unschedule](TorqueBox/Scheduling/Scheduler.html#unschedule-class_method)
   with the same `id` you used when scheduling the job:

    TorqueBox::Scheduling::Scheduler.unschedule(:job1)

2) call [Job#unschedule](TorqueBox/Scheduling/Job.html#unschedule-instance_method)
   on the job object returned by `Scheduler.schedule`:

    job = TorqueBox::Scheduling::Scheduler.schedule(:job1, every: 1000) do
      puts "I fire every second"
    end

    job.unschedule

Unscheduling a job will only unschedule it from the current node in a cluster.
