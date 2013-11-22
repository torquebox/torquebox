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
