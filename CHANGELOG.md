## 4.0.0.beta2

* The `TorqueBox::Logger` class now provides methods to instantiate
  new loggers and configure the underlying logging system.

## 4.0.0.beta1

* The 'torquebox' gem was missing gem dependencies on
  'torquebox-caching' and 'torquebox-messaging'. This has been fixed.

* Streaming of responses when not using chunked transfer-encoding is
  fixed. Previously, the response wouldn't be streamed and only get
  sent when the response was finished. This impacts Rail's
  response.stream, SSE, etc. Anything using chunked transfer-encoding
  worked fine and will continue to work fine.

* Bundler wasn't being packaged inside executable jars created with
  'torquebox jar' if Bundler was installed in a non-standard
  $GEM_HOME. The logic now looks at Gem.path instead of
  Gem.default_path, and thus should respect $GEM_HOME.

* Recent versions of Nokogiri will once again work when an app is
  packaged as a .war and deployed to WildFly.

* Passing "--env foo" to the "torquebox war" command no longer results
  in "NoMethodError: undefined method `[]=' for
  nil:Nilclass". Previously, this error would happen anytime you used
  the "--env" flag unless you also used the "--envvar FOO=BAR" flag to
  set some environment variable earlier in the command.

* Development moved to the 'master' branch instead of 'torqbox'

* The Rack Hijack API has been partially implemented. The only tested
  use of this is with the `tubesock` gem for WebSocket support.

* Print out the host and port that web is listening on when
  programmatically started with :auto_start set to true.

* Wars generated with 'torquebox war' can now be run directly with
  `java -jar foo.war`, just like jars.

* Scripts can now be run from inside packaged jar and war files using
  "-S".  Ex: `java -jar my_rails_app.jar -S rake db:migrate`

## 4.0.0.alpha1 - Big Changes

* The gem previous known as 'torqbox' is now 'torquebox-web'
  4.0.0.alpha1. The 'torquebox' gem pulls in the complete TorqueBox
  4.0 stack.

* The 'torqbox' command is now 'torquebox run'.

* Switched from bundling wunderboss-rack.jar to pulling in wunderboss
  and other necessary jars via maven.

* All the Rack components that used to exist in wunderboss were moved
  here, so the build was restructured to allow compiling java source
  files with maven dependencies without actually needing to use maven.

* Added --context-path option to 'torquebox' command to start web
  applications at a non-root context. In embedded mode this probably
  isn't all that useful outside of testing.

* Listen for SIGTERM (in addition to SIGINT) to know when to
  gracefully stop.

## 0.1.7

* Bundled wunderboss-rack commit
  fc2378dfe02394fa406e1aba1ac2efe07305cadd that upgrades to Undertow
  1.0.0.Beta30

## 0.1.6

* Bundled wunderboss-rack commit
  7bce2a980976cb215ff28548d2eef19c48a7410f to fix a bug preventing
  Rails 3.2 apps from working and some Ruby runtime creation/destory
  changes to get our specs passing again

## 0.1.5

* Bundled wunderboss-rack commit
  26ddebe347d5763f76dedeea9da62a95a78c7cb4 that lazily looks up Rack
  environment values as-needed, converts Rack response headers to Java
  response headers more efficiently, and simplifies the
  container/application API a bit

## 0.1.4

* Bundled wunderboss-rack commit
  d33c77329993c3697a265987c17ab72ecf751037 which improves performance
  of retrieving HTTP request headers

## 0.1.3

* Fix an issue where Rails application started via 'torqbox' would get
  'Bad file descriptor' errors when trying to write to Rails logs

* Bundled wunderboss-rack commit
  6cb09be797bbb265f35561d736692e2afcaea77d which fixes a bug in HTTP
  header parsing and adds Date and Server response headers


## 0.1.2

* Add torqbox -E <development/test/production> option to control the
  RACK_ENV and RAILS_ENV

* Bundled wunderboss-rack.jar commit
  4fcdf2c1dc9d5665b8f94e97dd0c93d8d64607be which improves web
  performance

## 0.1.1

* Fix an issue that prevented `rails s torqbox` from working because
  Rails thought the server stopped immediately after starting

* wunderboss-all.jar renamed to wunderboss-rack.jar, updated to commit
  498d5badcaefffa19e0c529caa62c490497f3079 which improves overall
  throughput by rewriting our Rack implementation from a Servlet
  adapter to an Undertow HttpHandler

## 0.1.0

* Initial release

* Bundled wunderboss-all.jar from projectodd/wunderboss commit
  23dd4ab2ca9321a7f57aa09c1dee041eaf1667c9
