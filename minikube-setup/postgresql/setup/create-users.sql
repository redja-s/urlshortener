-- Create database if it doesn't exist (idempotent)
SELECT 'CREATE DATABASE urls'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'urls')\gexec

-- Connect to the database
\c urls

-- Create dedicated user for url-shortener-service (FULL CRUD) - idempotent
DO
$$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_user WHERE usename = 'url_shortener_user') THEN
    CREATE USER url_shortener_user WITH PASSWORD 'url_shortener_secure_password';
    RAISE NOTICE 'Created user: url_shortener_user';
  ELSE
    RAISE NOTICE 'User already exists: url_shortener_user';
  END IF;
END
$$;

-- Grant database-level privileges to url_shortener_user (idempotent)
DO
$$
BEGIN
  GRANT CONNECT ON DATABASE urls TO url_shortener_user;
  RAISE NOTICE 'Granted database privileges to url_shortener_user';
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'Database privileges already granted to url_shortener_user';
END
$$;

-- Grant schema-level privileges INCLUDING CREATE (idempotent)
DO
$$
BEGIN
  GRANT USAGE ON SCHEMA public TO url_shortener_user;
  GRANT CREATE ON SCHEMA public TO url_shortener_user;  -- ✅ This is what was missing!
  RAISE NOTICE 'Granted schema privileges to url_shortener_user';
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'Schema privileges already granted to url_shortener_user';
END
$$;

-- Grant privileges on ALL existing tables (idempotent)
DO
$$
BEGIN
  GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO url_shortener_user;
  RAISE NOTICE 'Granted table privileges to url_shortener_user';
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'Table privileges already granted to url_shortener_user';
END
$$;

-- Grant privileges on ALL existing sequences (idempotent)
DO
$$
BEGIN
  GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO url_shortener_user;
  RAISE NOTICE 'Granted sequence privileges to url_shortener_user';
EXCEPTION WHEN OTHERS THEN
  RAISE NOTICE 'Sequence privileges already granted to url_shortener_user';
END
$$;

-- Grant default privileges for future objects (idempotent)
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO url_shortener_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT USAGE, SELECT ON SEQUENCES TO url_shortener_user;

-- Grant full privileges for objects url_shortener_user creates (idempotent)
ALTER DEFAULT PRIVILEGES FOR USER url_shortener_user IN SCHEMA public
GRANT ALL PRIVILEGES ON TABLES TO url_shortener_user;

ALTER DEFAULT PRIVILEGES FOR USER url_shortener_user IN SCHEMA public
GRANT ALL PRIVILEGES ON SEQUENCES TO url_shortener_user;

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
  GRANT CONNECT ON DATABASE urls TO redirect_user;
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
ALTER DEFAULT PRIVILEGES FOR USER url_shortener_user IN SCHEMA public
GRANT SELECT ON TABLES TO redirect_user;

-- Display completion message
\echo ''
\echo '✅ Database setup complete!'
\echo ''
\echo 'Created/verified users:'
\echo '  - url_shortener_user (FULL CRUD + CREATE TABLES)'
\echo '  - redirect_user (READ ONLY)'
\echo ''