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
