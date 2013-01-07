## WAT?

You can read more about the application in the [blog post](http://immutant.org/news/2012/12/11/openshift-postgresql). This is a Ruby version of the application, based on [Sinatra](http://www.sinatrarb.com/) and [TorqueBox](http://torquebox.org/).

## Twitter credentials

You need to use your Twitter OAuth credentials. You can generate required set of keys/secrets on the [Twitter apps page](https://dev.twitter.com/apps). Create a new application (or use existing) and modify `torquebox.rb` afterwards.

## Database

You need to prepare PostgreSQL database for the application.

### Create database

<pre>
$ su postgres -c psql 
psql (9.2.2)
Type "help" for help.

postgres=# CREATE USER poorsmatic WITH PASSWORD 'poorsmatic';
CREATE ROLE
postgres=# CREATE DATABASE poorsmatic;
CREATE DATABASE
postgres=# GRANT ALL PRIVILEGES ON DATABASE poorsmatic to poorsmatic;
GRANT
postgres=# \q
</pre>

### Authentication

Please make sure you have the appropriate authentication method enabled too. In `/var/lib/pgsql/data/pg_hba.conf` make sure authentication is set to `md5`, not `ident` (Fedora's default).

### Enable transactions

In `/var/lib/pgsql/data/postgresql.conf` please set `max_prepared_transactions` to value greater than `0`. I think `10` should be sufficient.

## License

[MIT](https://github.com/goldmann/poorsmatic/blob/master/LICENSE)
