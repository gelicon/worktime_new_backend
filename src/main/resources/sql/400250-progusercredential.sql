CREATE TABLE progusercredential(
	progusercredential_id INTEGER NOT NULL,
	progusercredential_password VARCHAR(128) NOT NULL,
	proguser_id INTEGER NOT NULL,
	progusercredential_type INTEGER NOT NULL,
	progusercredential_lockflag INTEGER NOT NULL,
	progusercredential_tempflag INTEGER NOT NULL,
    PRIMARY KEY (progusercredential_id)
);
CREATE SEQUENCE progusercredential_id_gen AS INTEGER START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE progusercredential_id_gen OWNED BY progusercredential.progusercredential_id;

