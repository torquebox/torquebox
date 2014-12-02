# @title Caching Guide

# Caching

TorqueBox caching is provided by the [Infinispan] data grid, the
distributed features of which are available when deployed to a
[WildFly] or EAP cluster. But even in "local mode", i.e. not in a
cluster but locally embedded within your app, Infinispan caches offer
features such as eviction, expiration, persistence, and transactions
that aren't available in typical [ConcurrentMap] implementations.

This guide will explore the
[TorqueBox::Caching](TorqueBox/Caching.html) module, which provides
access to Infinispan, whether your app is deployed to a WildFly/EAP
cluster or not.

## Creation and Configuration

Caches are created, started, and referenced using the
[TorqueBox::Caching.cache](TorqueBox/Caching.html#cache-class_method)
method. It accepts a number of optional configuration arguments, but
the only required one is a name, since every cache must be uniquely
named. If you pass the name of a cache that already exists, a
reference to the existing cache will be returned, effectively ignoring
any additional config options you might pass. So two cache instances
with the same name will be backed by the same Infinispan cache.

If you wish to reconfigure an existing cache, you must stop it first
by calling
[TorqueBox::Caching.stop](TorqueBox/Caching.html#stop-class_method).

Infinispan is a veritable morass of enterprisey configuration.
TorqueBox tries to strike a convention/configuration balance by
representing the more common options as kwargs passed to the `cache`
method, while still supporting the more esoteric config via
[TorqueBox::Caching.builder](TorqueBox/Caching.html#builder-class_method)
and Java interop.

## Example Usage

You're encouraged to run the following examples in an `irb` session:

### Writing

Let's create a cache and put some data in it:

    require 'torquebox-caching'
    c = TorqueBox::Caching.cache("foo")

    c.put(:a, 1)                                  #=> nil
    c.put(:a, 2)                                  #=> 1
    c[:b] = 3                                     #=> 3
    c.put_all(:x => 42, :y => 99)                 #=> nil

Note that `put` returns the previous value and `[]=` returns the new
one. We have all the [ConcurrentMap] operations at our disposal, too:

    # Cache it only if key doesn't exist
    c.put_if_absent(:b, 6)                        #=> 3
    c.put_if_absent(:d, 4)                        #=> nil

    # Cache it only if key exists
    c.replace(:e, 5)                              #=> nil
    c.replace(:b, 6)                              #=> 3

    # Cache it only if key and value exists
    c.compare_and_set(:b, 2, 0)                   #=> false
    c.compare_and_set(:b, 6, 0)                   #=> true

### Reading

Querying a cache is straightforward:

    c.get(:b)                                     #=> 0
    c[:b]                                         #=> 0
    c.size                                        #=> 5
    c.empty?                                      #=> false
    c.contains_key?(:b)                           #=> true
    c.cache                                       #=> {:y=>99, :x=>42, :a=>2, :d=>4, :b=>0}
    c.keys                                        #=> [:y, :x, :a, :d, :b]
    c.values                                      #=> [99, 42, 2, 4, 0]
    c.name                                        #=> "foo"

### Removing

Cache entries can be explicitly deleted using Java interop, but they
can also be subject to automatic expiration and eviction.

    # Removing a missing key is harmless
    c.remove(:missing)                            #=> nil

    # Removing an existing key returns its value
    c.remove(:b)                                  #=> 0

    # If value is passed, both must match for remove to succeed
    c.remove(:y, 8)                                #=> false
    c.remove(:y, 99)                               #=> true

    # Clear all entries
    c.clear

#### Expiration

By default, cached entries never expire, but you can trigger
expiration by passing the `:ttl` (time-to-live) and/or `:idle` options
to the `cache` method. Their units are milliseconds, and negative
values disable expiration.

If `:ttl` is specified, entries will be automatically deleted after
that amount of time elapses, starting from when the entry was added.
Effectively, this is the entry's "maximum lifespan". If `:idle` is
specified, the entry is deleted after the time elapses, but the
"timer" is reset each time the entry is accessed. If both are
specified, whichever elapses first "wins" and triggers expiration.

    # We can set the defaults for a cache
    e = TorqueBox::Caching.cache("bar", :ttl => 30*1000, :idle => 15*1000)

    # All of the cache manipulation methods take the same options
    e.put(:a, 42, :ttl => -1)
    e.put_all({:x => 42, :y => 99}, :idle => 60*1000)
    e.put_if_absent(:a, 42, :ttl => 100000)
    e.replace(:k, 99, :ttl => 500, :idle => 500)
    e.compare_and_set(:k, 99, 100, :ttl => 1000)
    
#### Eviction

To avoid memory exhaustion, you can include the `:max_entries` option
as well as the `:eviction` policy to determine which entries to evict.
And if the `:persist` option is set, evicted entries are not deleted
but rather flushed to disk so that the entries in memory are always a
finite subset of those on disk.

The default eviction policy is [:lirs], which is an optimized version
of `:lru` (Least Recently Used).

    baz = TorqueBox::Caching.cache "baz", :max_entries => 3
    baz[:a] = 1
    baz[:b] = 2
    baz[:c] = 3
    baz[:d] = 4
    baz[:a]                                       #=> nil
    baz.cache                                     #=> {:d=>4, :c=>3, :b=>2}

### Event Notification

Infinispan provides an API for registering callback functions to be
invoked when specific events occur during a cache's lifecycle.
Unfortunately, this API relies exclusively on Java annotations, which
are awkward in JRuby (as well as Java, if we're being honest).

Therefore, TorqueBox provides the ability to use standard Ruby symbols
and blocks to register interest in various types of cache lifecycle
events. For example, to print an event whenever an entry is either
visited or modified in the baz cache:

    result = baz.add_listener(:cache_entry_visited, :cache_entry_modified) {|e| puts e}

    baz.get_listeners.size                        #=> 2

    # This should show two messages for each event (before/after)
    baz[:b] = baz[:b] + 1

    # This should turn the notifications off
    result.each {|v| baz.remove_listener(v)}
    baz.get_listeners.empty?                      #=> true

### Encoding

Cache entries are encoded with modified version of Ruby's Marshal
codec called `:marshal_smart`. Other supported codecs include `:edn`,
`:json`, `:marshal`, `:marshal_base64` and `:text`

Setting the `:encoding` is typically necessary only when non-Ruby
clients are sharing your cache.

    edn = TorqueBox::Caching.cache("edn", :encoding => :edn)

## Clustering

Each Infinispan cache operates in one of four modes. Normally, *local*
mode is your only option, but when your app is deployed to a cluster,
you get three more: *invalidated*, *replicated*, and *distributed*.
These modes define how peers collaborate to replicate your data
throughout the cluster. Further, you can choose whether this
collaboration occurs asynchronous to the write.

* `:local` This is the only supported mode outside of a cluster
* `:dist_sync` `:dist_async` This mode enables Infinispan caches to
  achieve "linear scalability". Cache entries are copied to a fixed
  number of peers (2, by default) regardless of the cluster size.
  Distribution uses a consistent hashing algorithm to determine which
  nodes will store a given entry.
* `:invalidation_sync` `:invalidation_async` No data is actually
  shared among the cluster peers in this mode. Instead, notifications
  are sent to all nodes when data changes, causing them to evict their
  stale copies of the updated entry.
* `:repl_sync` `:repl_async` In this mode, entries added to any peer
  will be copied to all other peers in the cluster, and can then be
  retrieved locally from any instance. This mode is probably
  impractical for clusters of any significant size. Infinispan
  recommends 10 as a reasonable upper bound on the number of
  replicated nodes.

The simplest way to take advantage of Infinispan's clustering
capabilities is to deploy your app to a [WildFly] cluster.

[Infinispan]: http://infinispan.org
[ConcurrentMap]: http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ConcurrentMap.html
[WildFly]: file.wildfly.html
[:lirs]: http://en.wikipedia.org/wiki/LIRS_caching_algorithm
