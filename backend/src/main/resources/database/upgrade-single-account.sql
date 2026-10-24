-- One-time, non-destructive upgrade of an existing T03 database.
-- Run explicitly as the existing Docker bootstrap administrator, not through Flyway.
-- Only the current database and named application tables change ownership.
\set ON_ERROR_STOP on
\getenv database_name POSTGRES_DB
BEGIN;
ALTER DATABASE :"database_name" OWNER TO meal_planner_app;
ALTER TABLE public.ingredient OWNER TO meal_planner_app;
ALTER TABLE public.ingredient_alias OWNER TO meal_planner_app;
ALTER TABLE public.retailer OWNER TO meal_planner_app;
ALTER TABLE public.product OWNER TO meal_planner_app;
ALTER TABLE public.ingredient_product_mapping OWNER TO meal_planner_app;
ALTER TABLE public.flyway_schema_history OWNER TO meal_planner_app;
-- PostgreSQL also transfers the tables' dependent identity sequences.
COMMIT;
