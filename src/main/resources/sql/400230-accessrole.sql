CREATE TABLE accessrole(
	accessrole_id INTEGER NOT NULL,
	accessrole_name VARCHAR(30) NOT NULL,
	accessrole_note VARCHAR(255),
	accessrole_visible INTEGER NOT NULL,
    PRIMARY KEY (accessrole_id)
);
CREATE SEQUENCE accessrole_id_gen AS INTEGER START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE accessrole_id_gen OWNED BY accessrole.accessrole_id;

CREATE VIEW ft_accessrole
AS
SELECT
	accessrole_id,
	CAST(
		trim(accessrole_name)||' '||coalesce(accessrole_note,'')
		AS varchar(305)
	) AS fulltext
FROM accessrole;

