-- Upgrade script from ART 4.11 to ART 4.12

-- CHANGES:
-- update database version

-- ------------------------------------------------


-- update database version
UPDATE ART_DATABASE_VERSION SET DATABASE_VERSION='4.12-snapshot';
