Requirements
------------
* Maven 3

Building
--------
Simply type

    mvn install

Layout
------

Generally speaking:

* `*-support`: Supports the build itself
* `foo-spi`: Interfaces for service *foo*
* `foo-core`: Implementation for service *foo*
* `foo-metadata`: Configuration metadata for service *foo*
* `foo-int`: VDF integration for service *foo*

