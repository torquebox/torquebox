
## torquebox fatjar

- rename to torquebox jar

- let users specify the extract directory? or just have them set
  java.io.tmpdir?

- TorqueBoxMain needs to intercept -h/--help and display useful
  information, including how to run bare jruby commands via 'java -jar
  foo.jar jruby ...'

## web

- add standard websocket, non-sockjs support

- figure out if we can get faye and generic rack hijack io working as
  expected - may need changes to eventmachine, since that's where most
  people hand off the rack hijack io

## messaging

- figure out what API we want to expose, implement wrapper on top of wunderboss API
