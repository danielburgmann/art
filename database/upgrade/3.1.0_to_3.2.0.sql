-- Upgrade script from ART 3.1 to ART 3.2

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='3.2-snapshot';