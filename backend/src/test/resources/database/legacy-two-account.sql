-- Reproduce the previously shipped T03 bootstrap for compatibility testing.
\getenv app_password APP_DB_PASSWORD
\getenv database_name POSTGRES_DB
CREATE ROLE meal_planner_app LOGIN PASSWORD :'app_password';
REVOKE ALL ON DATABASE :"database_name" FROM PUBLIC;
GRANT CONNECT ON DATABASE :"database_name" TO meal_planner_app;
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
