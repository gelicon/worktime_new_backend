CREATE TABLE controlobject(
	controlobject_id INTEGER NOT NULL,
	controlobject_name VARCHAR(128) NOT NULL,
	controlobject_url VARCHAR(255) NOT NULL,
    PRIMARY KEY (controlobject_id)
);
CREATE SEQUENCE controlobject_id_gen AS INTEGER START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE controlobject_id_gen OWNED BY controlobject.controlobject_id;

CREATE VIEW ft_controlobject
AS
SELECT
	controlobject_id,
	CAST(
		trim(controlobject_name)||' '||trim(controlobject_url)
		AS varchar(393)
	) AS fulltext
FROM controlobject;

