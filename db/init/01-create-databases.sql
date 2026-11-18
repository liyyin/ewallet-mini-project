-- Satu container MySQL, dua database (satu per service)
CREATE DATABASE user_db;
CREATE DATABASE wallet_db;

GRANT ALL PRIVILEGES ON user_db.*   TO 'app'@'%';
GRANT ALL PRIVILEGES ON wallet_db.* TO 'app'@'%';
FLUSH PRIVILEGES;
