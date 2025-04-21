<?php

namespace App\Services;

use PDO;
use PDOException;
use App\Utils\Logger;

/**
 * Database Service
 * 
 * This service provides a centralized interface for database operations
 * with support for multiple database drivers (MySQL, PostgreSQL, SQLite).
 * It handles connection management, transaction control, and query execution.
 * 
 * Features:
 * - Lazy loading of database connections
 * - Support for multiple database drivers (MySQL, PostgreSQL, SQLite)
 * - Transaction management
 * - Prepared statement execution with parameter binding
 * - Detailed error logging
 * - Connection pooling with singleton pattern
 * 
 * @package App\Services
 */
class Database
{
    /**
     * PDO connection instance
     * 
     * Shared database connection that is lazily initialized
     * 
     * @var PDO|null The PDO instance
     */
    private static ?PDO $pdo = null;
    
    /**
     * SQL function call syntax based on database type
     * 
     * Different database systems have different syntax for calling
     * stored procedures (MySQL uses CALL, PostgreSQL and SQLite use SELECT)
     * 
     * @var string SQL function call method based on database type
     */
    private static string $sqlFunctionCallMethod = 'select ';
    
    /**
     * Get the PDO instance
     * 
     * Returns the existing PDO instance or creates a new one if none exists.
     * Implements lazy loading of the database connection.
     * 
     * @return PDO Active database connection
     */
    public static function getPdo(): PDO
    {
        if (self::$pdo === null) {
            self::connect();
        }
        
        return self::$pdo;
    }
    
    /**
     * Get the SQL function call method
     * 
     * Returns the appropriate syntax for calling stored procedures
     * in the current database system.
     * 
     * @return string SQL function call method ('CALL ' for MySQL, 'select ' for others)
     */
    public static function getSqlFunctionCallMethod(): string
    {
        if (self::$pdo === null) {
            self::connect();
        }
        
        return self::$sqlFunctionCallMethod;
    }
    
    /**
     * Connect to the database
     * 
     * Establishes a connection to the database using configuration settings.
     * The connection parameters are read from the application configuration.
     * 
     * @throws PDOException If connection fails due to invalid credentials or server issues
     * @return void
     */
    public static function connect(): void
    {
        $driver = config('database.driver', 'sqlite');
        $options = config('database.options', [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
        ]);
        
        try {
            switch ($driver) {
                case 'mysql':
                    $host = config('database.host', 'localhost');
                    $port = config('database.port', '3306');
                    $dbname = config('database.database', 'gpstracker');
                    $username = config('database.username', 'gpstracker_user');
                    $password = config('database.password', 'gpstracker');
                    $charset = config('database.charset', 'utf8mb4');
                    
                    self::$pdo = new PDO(
                        "mysql:host={$host};port={$port};dbname={$dbname};charset={$charset}",
                        $username,
                        $password,
                        $options
                    );
                    
                    self::$sqlFunctionCallMethod = 'CALL ';
                    break;
                    
                case 'postgresql':
                    $host = config('database.host', 'localhost');
                    $port = config('database.port', '5432');
                    $dbname = config('database.database', 'gpstracker');
                    $username = config('database.username', 'gpstracker_user');
                    $password = config('database.password', 'gpstracker');
                    
                    self::$pdo = new PDO(
                        "pgsql:host={$host};port={$port};dbname={$dbname}",
                        $username,
                        $password,
                        $options
                    );
                    
                    self::$sqlFunctionCallMethod = 'select ';
                    break;
                    
                case 'sqlite':
                default:
                    $path = config('database.sqlite_path', __DIR__ . '/../../sqlite/gpstracker.sqlite');
                    
                    self::$pdo = new PDO("sqlite:{$path}", null, null, $options);
                    self::$sqlFunctionCallMethod = 'select ';
                    break;
            }
            
            Logger::info('Database connection established', ['driver' => $driver]);
        } catch (PDOException $e) {
            Logger::error('Database connection failed', [
                'message' => $e->getMessage(),
                'driver' => $driver,
            ]);
            
            throw $e;
        }
    }
    
    /**
     * Close the database connection
     * 
     * Explicitly closes the database connection and releases resources.
     * 
     * @return void
     */
    public static function disconnect(): void
    {
        self::$pdo = null;
    }
    
    /**
     * Begin a transaction
     * 
     * Starts a database transaction for a series of operations
     * that should be treated as a single atomic unit.
     * 
     * @return bool True on success, false on failure
     */
    public static function beginTransaction(): bool
    {
        return self::getPdo()->beginTransaction();
    }
    
    /**
     * Commit a transaction
     * 
     * Permanently applies all changes made during the current transaction
     * and ends the transaction.
     * 
     * @return bool True on success, false on failure
     */
    public static function commit(): bool
    {
        return self::getPdo()->commit();
    }
    
    /**
     * Rollback a transaction
     * 
     * Discards all changes made during the current transaction
     * and ends the transaction.
     * 
     * @return bool True on success, false on failure
     */
    public static function rollback(): bool
    {
        return self::getPdo()->rollBack();
    }
    
    /**
     * Execute a query with parameters
     * 
     * Executes an SQL query with parameter binding and returns all result rows.
     * Uses prepared statements for SQL injection protection.
     * 
     * @param string $query SQL query with named parameter placeholders
     * @param array $params Named parameters to bind to the query
     * @return array Results as an array of associative arrays
     * @throws PDOException If the query fails
     */
    public static function query(string $query, array $params = []): array
    {
        try {
            $stmt = self::getPdo()->prepare($query);
            $stmt->execute($params);
            
            return $stmt->fetchAll();
        } catch (PDOException $e) {
            Logger::error('Database query failed', [
                'query' => $query,
                'params' => $params,
                'message' => $e->getMessage(),
            ]);
            
            throw $e;
        }
    }
    
    /**
     * Execute a query and return a single row
     * 
     * Executes an SQL query and returns only the first result row.
     * Useful for queries that are expected to return a single record.
     * 
     * @param string $query SQL query with named parameter placeholders
     * @param array $params Named parameters to bind to the query
     * @return array|null Result row as an associative array or null if no results
     * @throws PDOException If the query fails
     */
    public static function queryOne(string $query, array $params = []): ?array
    {
        $result = self::query($query, $params);
        
        return !empty($result) ? $result[0] : null;
    }
    
    /**
     * Execute a query and return a single column value
     * 
     * Executes an SQL query and returns only the first column of the first row.
     * Useful for queries that are expected to return a single value.
     * 
     * @param string $query SQL query with named parameter placeholders
     * @param array $params Named parameters to bind to the query
     * @return mixed Column value or false if no results
     * @throws PDOException If the query fails
     */
    public static function queryColumn(string $query, array $params = [])
    {
        try {
            $stmt = self::getPdo()->prepare($query);
            $stmt->execute($params);
            
            return $stmt->fetchColumn();
        } catch (PDOException $e) {
            Logger::error('Database query failed', [
                'query' => $query,
                'params' => $params,
                'message' => $e->getMessage(),
            ]);
            
            throw $e;
        }
    }
    
    /**
     * Execute an insert query and return the last insert ID
     * 
     * Executes an SQL INSERT statement and returns the ID of the newly inserted row.
     * The behavior of lastInsertId depends on the database driver.
     * 
     * @param string $query SQL INSERT query with named parameter placeholders
     * @param array $params Named parameters to bind to the query
     * @return string|int Last insert ID (string for compatibility with all drivers)
     * @throws PDOException If the insert fails
     */
    public static function insert(string $query, array $params = [])
    {
        try {
            $stmt = self::getPdo()->prepare($query);
            $stmt->execute($params);
            
            return self::getPdo()->lastInsertId();
        } catch (PDOException $e) {
            Logger::error('Database insert failed', [
                'query' => $query,
                'params' => $params,
                'message' => $e->getMessage(),
            ]);
            
            throw $e;
        }
    }
    
    /**
     * Execute an update query and return the number of affected rows
     * 
     * Executes an SQL UPDATE statement and returns the number of rows that were modified.
     * 
     * @param string $query SQL UPDATE query with named parameter placeholders
     * @param array $params Named parameters to bind to the query
     * @return int Number of affected rows
     * @throws PDOException If the update fails
     */
    public static function update(string $query, array $params = []): int
    {
        try {
            $stmt = self::getPdo()->prepare($query);
            $stmt->execute($params);
            
            return $stmt->rowCount();
        } catch (PDOException $e) {
            Logger::error('Database update failed', [
                'query' => $query,
                'params' => $params,
                'message' => $e->getMessage(),
            ]);
            
            throw $e;
        }
    }
    
    /**
     * Execute a delete query and return the number of affected rows
     * 
     * Executes an SQL DELETE statement and returns the number of rows that were removed.
     * 
     * @param string $query SQL DELETE query with named parameter placeholders
     * @param array $params Named parameters to bind to the query
     * @return int Number of affected rows (0 if no matching records found)
     * @throws PDOException If the delete fails
     */
    public static function delete(string $query, array $params = []): int
    {
        try {
            error_log("!!! Database::delete executing query: " . $query . " with params: " . json_encode($params));
            $stmt = self::getPdo()->prepare($query);
            $stmt->execute($params);
            
            return $stmt->rowCount();
        } catch (PDOException $e) {
            Logger::error('Database delete failed', [
                'query' => $query,
                'params' => $params,
                'message' => $e->getMessage(),
            ]);
            
            throw $e;
        }
    }
}
