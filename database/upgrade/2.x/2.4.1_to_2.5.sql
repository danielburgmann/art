-- Upgrade script from ART 2.4.1 to ART 2.5
--
-- Purpose: create/update the tables needed to 
--          . update database version
--          . Change "admin level" for users to "access level"
--          . rename "object" to "query"
--          . rename art_shared_jobs table to art_user_jobs
--          . support job archives
--          . support for running DDL/DML statements before query's select statement
--          . support start query
--
-- ------------------------------------------------


-- update database version 
UPDATE ART_SETTINGS SET SETTING_VALUE='2.5' WHERE SETTING_NAME='database version';

-- Change "admin level" for users to "access level"
ALTER TABLE ART_USERS ADD ACCESS_LEVEL INTEGER;
UPDATE ART_USERS SET ACCESS_LEVEL=ADMIN_LEVEL;
ALTER TABLE ART_USERS DROP COLUMN ADMIN_LEVEL;

-- rename "object" to "query"
ALTER TABLE ART_USERS ADD DEFAULT_QUERY_GROUP INTEGER;
UPDATE ART_USERS SET DEFAULT_QUERY_GROUP=DEFAULT_OBJECT_GROUP;
ALTER TABLE ART_USERS DROP COLUMN DEFAULT_OBJECT_GROUP;

ALTER TABLE ART_USER_GROUPS ADD DEFAULT_QUERY_GROUP INTEGER;
UPDATE ART_USER_GROUPS SET DEFAULT_QUERY_GROUP=DEFAULT_OBJECT_GROUP;
ALTER TABLE ART_USER_GROUPS DROP COLUMN DEFAULT_OBJECT_GROUP;

ALTER TABLE ART_LOGS ADD QUERY_ID INTEGER;
UPDATE ART_LOGS SET QUERY_ID=OBJECT_ID;
ALTER TABLE ART_LOGS DROP COLUMN OBJECT_ID;

-- rename art_shared_jobs table to art_user_jobs
CREATE TABLE ART_USER_JOBS
(
	JOB_ID INTEGER NOT NULL,
	USERNAME VARCHAR(30) NOT NULL,
	USER_GROUP_ID INTEGER,
	LAST_FILE_NAME VARCHAR(4000),
	LAST_START_DATE TIMESTAMP NULL,
	LAST_END_DATE TIMESTAMP NULL,
	PRIMARY KEY (JOB_ID,USERNAME)	
);

INSERT INTO ART_USER_JOBS (JOB_ID, USERNAME, USER_GROUP_ID, LAST_FILE_NAME, LAST_START_DATE, LAST_END_DATE)
SELECT JOB_ID, USERNAME, USER_GROUP_ID, LAST_FILE_NAME, LAST_START_DATE, LAST_END_DATE FROM ART_SHARED_JOBS;

-- support job archives
ALTER TABLE ART_JOBS ADD RUNS_TO_ARCHIVE INTEGER;

CREATE TABLE ART_JOB_ARCHIVES
(
	ARCHIVE_ID VARCHAR(100) NOT NULL PRIMARY KEY,
	JOB_ID INTEGER NOT NULL,
	USERNAME VARCHAR(30) NOT NULL,	
	ARCHIVE_FILE_NAME VARCHAR(4000),
	START_DATE TIMESTAMP NULL,
	END_DATE TIMESTAMP NULL,
	JOB_SHARED VARCHAR(1)
);

-- support for running DDL/DML statements before query's select statement
ALTER TABLE ART_QUERIES ADD DISPLAY_RESULTSET INTEGER;

-- support start query
ALTER TABLE ART_USERS ADD START_QUERY VARCHAR(500);
ALTER TABLE ART_USER_GROUPS ADD START_QUERY VARCHAR(500);



