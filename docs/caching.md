# @title Caching Guide

# Caching

TorqueBox caching is provided by the [Infinispan] data grid, the
distributed features of which are available when deployed to a WildFly
or EAP cluster. But even in "local mode", i.e. not in a cluster but
locally embedded within your app, Infinispan caches offer features
such as eviction, expiration, persistence, and transactions that
aren't available in typical [ConcurrentMap] implementations.

This guide will explore the [TorqueBox::Caching] module, which
provides access to Infinispan, whether your app is deployed to a
WildFly/EAP cluster or not.

## Creation and Configuration

Caches are created, started, and referenced using the
[TorqueBox::Caching::cache] method. It accepts a number of optional
configuration arguments, but the only required one is a name, since
every cache must be uniquely named. If you pass the name of a cache
that already exists, a reference to the existing cache will be
returned, effectively ignoring any additional config options you might
pass. So two cache instances with the same name will be backed by the
same Infinispan cache.

If you wish to reconfigure an existing cache, you must stop it first
by calling [TorqueBox::Caching::stop].

Infinispan is a veritable morass of enterprisey configuration.
TorqueBox tries to strike a convention/configuration balance by
representing the more common options as kwargs passed to the `cache`
method, while still supporting the more esoteric config via
[TorqueBox::Caching::builder] and Java interop.

## Example Usage

You're encouraged to run the following examples in an `irb` session:

### Writing

### Reading

### Removing

#### Expiration

#### Eviction

### Event Notification

### Encoding

## Clustering
