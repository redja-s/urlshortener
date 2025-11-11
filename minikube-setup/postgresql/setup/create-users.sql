-- Create database if it doesn't exist (idempotent)
SELECT 'CREATE DATABASE urlshortener'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'urlshortener')\gexec

-- Connect to the database
\c urlshortener

-- Create dedicated user for url-shortener-service (FULL CRUD) - idempotent
DO
$$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'shortener_user') THEN
    CREATE USER shortener_user WITH PASSWORD 'shortener_secure_password';
    RAISE NOTICE 'Created user: shortener_user';
  ELSE
    RAISE NOTICE 'User already exists: shortener_user';
  END IF;
END
$$;

-- Grant database-level privileges to shortener_user (idempotent)
DO
$$
BEGIN
  GRANT CONNECT ON DATABASE urlshortener TO shortener_user;
  RAISE NOTICE 'Granted database privileges to shortener_user';
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'Database privileges already granted to shortener_user';
END
$$;

-- Grant schema-level privileges INCLUDING CREATE (idempotent)
DO
$$
BEGIN
  GRANT USAGE ON SCHEMA public TO shortener_user;
  GRANT CREATE ON SCHEMA public TO shortener_user;  -- ✅ This is what was missing!
  RAISE NOTICE 'Granted schema privileges to shortener_user';
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'Schema privileges already granted to shortener_user';
END
$$;

-- Grant privileges on ALL existing tables (idempotent)
DO
$$
BEGIN
  GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO shortener_user;
  RAISE NOTICE 'Granted table privileges to shortener_user';
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'Table privileges already granted to shortener_user';
END
$$;

-- Grant privileges on ALL existing sequences (idempotent)
DO
$$
BEGIN
  GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO shortener_user;
  RAISE NOTICE 'Granted sequence privileges to shortener_user';
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'Sequence privileges already granted to shortener_user';
END
$$;

-- Grant default privileges for future objects (idempotent)
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO shortener_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT USAGE, SELECT ON SEQUENCES TO shortener_user;

-- Grant full privileges for objects shortener_user creates (idempotent)
ALTER DEFAULT PRIVILEGES FOR USER shortener_user IN SCHEMA public
GRANT ALL PRIVILEGES ON TABLES TO shortener_user;

ALTER DEFAULT PRIVILEGES FOR USER shortener_user IN SCHEMA public
GRANT ALL PRIVILEGES ON SEQUENCES TO shortener_user;

-- Create read-only user for redirect-service (idempotent)
DO
$$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'redirect_user') THEN
    CREATE USER redirect_user WITH PASSWORD 'redirect_secure_password';
    RAISE NOTICE 'Created user: redirect_user';
  ELSE
    RAISE NOTICE 'User already exists: redirect_user';
  END IF;
END
$$;

-- Grant database connection to redirect_user (idempotent)
DO
$$
BEGIN
  GRANT CONNECT ON DATABASE urlshortener TO redirect_user;
  RAISE NOTICE 'Granted database privileges to redirect_user';
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'Database privileges already granted to redirect_user';
END
$$;

-- Grant schema usage to redirect_user (idempotent)
DO
$$
BEGIN
  GRANT USAGE ON SCHEMA public TO redirect_user;
  RAISE NOTICE 'Granted schema privileges to redirect_user';
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'Schema privileges already granted to redirect_user';
END
$$;

-- Grant read-only access to ALL existing tables (idempotent)
DO
$$
BEGIN
  GRANT SELECT ON ALL TABLES IN SCHEMA public TO redirect_user;
  RAISE NOTICE 'Granted table privileges to redirect_user';
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'Table privileges already granted to redirect_user';
END
$$;

-- Grant read-only access to future tables (idempotent)
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT ON TABLES TO redirect_user;

-- Display completion message
\echo ''
\echo '✅ Database setup complete!'
\echo ''
\echo 'Created/verified users:'
\echo '  - shortener_user (FULL CRUD + CREATE TABLES)'
\echo '  - redirect_user (READ ONLY)'
\echo ''