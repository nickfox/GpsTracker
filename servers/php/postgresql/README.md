
#### PHP and PostgreSQL

Just like the MySQL version, using PostgreSQL with GpsTracker requires a web server, such as Apache, and PHP.  Once you have your server software installed, you need to create a website on your Apache web server and create a directory called gpstracker.  Put all of the files from the php download directory into there.

Now install the PostgreSQL database server.  You may want to install the GUI admin tool, pgAdmin III, but it is not required if you are comfortable using the command line.


##### Creating the GpsTracker Database Using the PostgreSQL Command Line

Open a command window, and change directory to Postgresql\bin:
<pre>
	cd "C:\Program Files\PostgreSQL\9.4\bin"
</pre>

Now create a database user for inserting and quering the data:
<pre>
	C:\Program Files\PostgreSQL\9.4\bin>psql -U postgres -c "CREATE USER gpstracker_user WITH PASSWORD 'gpstracker';"
	Password for user postgres:
	CREATE ROLE

	C:\Program Files\PostgreSQL\9.4\bin>psql -U postgres -c "CREATE DATABASE gpstracker;"
	Password for user postgres:
	CREATE DATABASE

	C:\Program Files\PostgreSQL\9.4\bin>psql -U postgres -c "ALTER DATABASE gpstracker OWNER TO gpstracker_user;"
	Password for user postgres:
	ALTER DATABASE

	C:\Program Files\PostgreSQL\9.4\bin>psql -U gpstracker_user -d gpstracker -f C:\Users\brent\Documents\gpstracker-09-14-14.sql
	Password for user gpstracker_user:
	psql:C:/Users/brent/Documents/gpstracker-09-14-14.sql:6: NOTICE:  view "v_getallroutesformap" does not exist, skipping DROP VIEW
	psql:C:/Users/brent/Documents/gpstracker-09-14-14.sql:7: NOTICE:  view "v_getrouteformap" does not exist, skipping DROP VIEW
	psql:C:/Users/brent/Documents/gpstracker-09-14-14.sql:8: NOTICE:  view "v_getroutes" does not exist, skipping DROP VIEW
	psql:C:/Users/brent/Documents/gpstracker-09-14-14.sql:9: NOTICE:  index "sessionidindex" does not exist, skipping DROP INDEX
	psql:C:/Users/brent/Documents/gpstracker-09-14-14.sql:10: NOTICE:  index "phonenumberindex" does not exist, skipping DROP INDEX
	psql:C:/Users/brent/Documents/gpstracker-09-14-14.sql:11: NOTICE:  index "usernameindex" does not exist, skipping DROP INDEX
	psql:C:/Users/brent/Documents/gpstracker-09-14-14.sql:13: NOTICE:  table "gpslocations" does not exist, skipping DROP TABLE
	CREATE TABLE
	CREATE INDEX
	CREATE INDEX
	CREATE INDEX
	INSERT 0 9
	CREATE VIEW
	CREATE VIEW
	CREATE VIEW
</pre>


Now edit GpsTracker's PHP file "dbconnect.php: to set the database type to PostgreSQL:
<pre>
$dbType = DB_POSTGRESQL;
</pre>

That's it!  