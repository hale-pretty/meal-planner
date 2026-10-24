-- Create the non-superuser application owner; do not echo its password.
\getenv app_password APP_DB_PASSWORD
\getenv database_name POSTGRES_DB
CREATE ROLE meal_planner_app LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS PASSWORD :'app_password';
ALTER DATABASE :"database_name" OWNER TO meal_planner_app;
REVOKE ALL ON DATABASE :"database_name" FROM PUBLIC;
GRANT CONNECT ON DATABASE :"database_name" TO meal_planner_app;
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
