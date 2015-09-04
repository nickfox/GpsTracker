
#### PHP and SQLite

Unlike using a client-server database (such as MySQL, PostgreSQL, or SQL Server), using SQLite with GpsTracker does not require installing any database server software.  It does still require a web server, such as Apache, and PHP scripting.  You need to create a website on your Apache web server and create a directory called gpstracker.  Put all of the files from the php download directory into there.

The SQLite subdirectory inside the PHP directory contains a gpstracker.sqlite file with the table and views already setup and is read to go!  Open GpsTracker's PHP file "dbconnect.php" to set the database type to SQLite:
<pre>
$dbType = DB_SQLITE3;
</pre>

That's it!  