# @title Messaging Guide

# Messaging

TorqueBox provides support for asynchronous and synchronous messaging.

The API for messaging all resides within the
[Messaging module](TorqueBox/Messaging.html).

## The gem

Messaging services are provided by the `torquebox-messaging` gem, and
can be used independently of other TorqueBox services.

## The API

The messaging API is backed by [HornetQ], which is an implementation
of [JMS]. JMS provides two primary destination types: *queues* and
*topics*. Queues represent point-to-point destinations, and topics
publish/subscribe.

To use a destination, we need to get a reference to one via the
[Messaging.queue](TorqueBox/Messaging.html#queue-class_method) or
[Messaging.topic](TorqueBox/Messaging.html#topic-class_method) methods
(or similar methods on a [Context](TorqueBox/Messaging/Context.html)
for remote destinations), depending on the type required. This will
create the destination if it does not already exist.

Once we have a reference to a destination, we can operate on it with
the following methods:

* [Destination.publish](TorqueBox/Messaging/Destination.html#publish-instance_method) -
  sends a message to the destination
* [Destination.receive](TorqueBox/Messaging/Destination.html#receive-instance_method) -
  receives a single message from the destination
* [Destination.listen](TorqueBox/Messaging/Destination.html#listen-instance_method) -
  registers a function to be called each time a message arrives at the
  destination

If the destination is a queue, we can do synchronous messaging
([request-response]):

* [Queue.respond](TorqueBox/Messaging/Queue.html#respond-instance_method) -
  registers a function that receives each request, and the returned
  value will be sent back to the requester
* [Queue.request](TorqueBox/Messaging/Queue.html#request-instance_method) -
  sends a message to the responder

### Some Examples

First, let's create a queue:

    TorqueBox::Messaging.queue("my-queue")

That will create the queue in the HornetQ broker for us. We'll need a
reference to that queue to operate on it. Let's go ahead and store
that reference:

    q = TorqueBox::Messaging.queue("my-queue")

We can call `queue` any number of times - if the queue already exists,
we're just grabbing a reference to it.

Now, let's register a listener on our queue. Let's just print every
message we get:

    listener = q.listen { |m| puts m }

We can publish to that queue, and see that the listener gets called:

    q.publish(:hi => :there)

You'll notice that we're publishing a hash there - we can publish
pretty much any data structure as a message. By default, that message
will be encoded using Ruby's built-in Marshal format. We also support
other encodings, namely: `:edn`, `:json`, `:marshal_base64` and
`:test`. We can choose a different encoding by passing an :encoding
option to `publish`:

    q.publish({:hi => :there}, :encoding => :json)

If you want to use `:json`, you'll need to have the JSON gem loaded.

We can deregister the listener by calling `.close` on it:

    listener.close

Now let's take a look at synchronous messaging. Let's create a new
queue for this (you'll want to use a dedicated queue for each
responder) and register a responder that just increments the request:

    sync_q = TorqueBox::Messaging.queue("sync")

    responder = sync_q.respond { |m| m.succ }

Then, we make a request, which blocks and waits for a response:

    sync_q.request(1)

The responder is just a fancy listener, and can be deregistered the
same way as a listener:

    responder.close

## Remote contexts

To connect to a remote HornetQ instance, you'll need to create a
remote context by instantiating a
[Context](TorqueBox/Messaging/Context.html), and use it when getting a
reference to the destination:

    TorqueBox::Messaging::Context.new(:host => "some-host", :port => 5445) do |context|
      context.queue("foo").publish("a message")
    end

A few things to note about the above example:

* We're passing a block to the `Context` initializer, which will
  ensure the context gets closed when the block completes.
* We're acquiring the `Queue` reference from the context, which
  just returns a reference to the remote queue, *without* asking
  HornetQ to create it. You'll need to make sure it already exists.

## Reusing contexts

By default, TorqueBox creates a new context object for each `publish`,
`request` or `receive` call. Creating a context isn't free, and incurs
some performance overhead. If you plan on calling any of those
functions in a tight loop, you can gain some performance by creating
the context yourself:

    q = TorqueBox::Messaging.queue("foo")
    TorqueBox::Messaging::Context.new do |context|
      10000.times do |n|
        q.publish(n, :context => context)
      end
    end

## HornetQ configuration

When used outside of WildFly, we configure [HornetQ] via a pair of xml
files. If you need to adjust any of the HornetQ
[configuration options], you can provide a copy of one (or both) of
those files (`hornetq-configuration.xml` and `hornetq-jms.xml`, which
should be based off of the [default versions]) on your application's
classpath and your copies will be used instead of the default
ones. When making changes to these files, be careful about changing
existing settings, as TorqueBox relies on some of them.

We've also exposed a few HornetQ settings as system properties, namely:

| Property             | Description                                                 | Default           |
|----------------------|-------------------------------------------------------------|-------------------|
| `hornetq.data.dir`   | The base directory for HornetQ to store its data files      | `./hornetq-data/` |
| `hornetq.netty.port` | The port that HornetQ will listen on for remote connections | `5445`            |
| `hornetq.netty.host` | The host that HornetQ will listen on for remote connections | `localhost`       |

Note that any custom xml or system properties will be ignored when
running inside WildFly - you'll need to make adjustments to the
WildFly configuration to achieve similar effects.

## More to come

That was just a brief introduction to the messaging API. There are
features we've yet to cover (durable topic subscriptions,
transactional sessions)...

[HornetQ]: http://hornetq.jboss.org/
[JMS]: https://en.wikipedia.org/wiki/Java_Message_Service
[request-response]: https://en.wikipedia.org/wiki/Request-response
[default versions]: https://github.com/projectodd/wunderboss/blob/0.2.0/modules/messaging/src/main/resources/
[configuration options]: https://docs.jboss.org/hornetq/2.4.0.Final/docs/user-manual/html_single/#server.configuration
