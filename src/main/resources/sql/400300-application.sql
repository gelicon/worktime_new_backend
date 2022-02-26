CREATE TABLE application(
	application_id INTEGER NOT NULL,
	application_type INTEGER NOT NULL,
	application_code VARCHAR(50),
	application_name VARCHAR(50) NOT NULL,
	application_exe VARCHAR(255) NOT NULL,
	application_blob bytea,
	application_desc VARCHAR(255),
    CONSTRAINT application_pk PRIMARY KEY (application_id),
    CONSTRAINT application_ak1 UNIQUE (application_name)
);
CREATE SEQUENCE application_id_gen AS INTEGER START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE application_id_gen OWNED BY application.application_id;

CREATE VIEW ft_application
AS
SELECT
	application_id,
	CAST(
		coalesce(application_code,'')||' '||trim(application_name)||' '||trim(application_exe)||' '||coalesce(application_desc,'')
		AS varchar(630)
	) AS fulltext
FROM application;

