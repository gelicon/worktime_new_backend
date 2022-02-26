CREATE TABLE applicationrole (
    applicationrole_id INTEGER NOT NULL,
    accessrole_id INTEGER NOT NULL,
    application_id INTEGER NOT NULL,
    CONSTRAINT applicationrole_ak1 UNIQUE (accessrole_id, application_id),
    CONSTRAINT applicationrole_pk PRIMARY KEY (applicationrole_id),
    CONSTRAINT applicationrole_fk1 FOREIGN KEY (accessrole_id) REFERENCES accessrole(accessrole_id),
    CONSTRAINT applicationrole_fk2 FOREIGN KEY (application_id) REFERENCES application(application_id)
);
/*
CREATE INDEX applicationrole_if1 ON applicationrole USING btree (accessrole_id);
CREATE INDEX applicationrole_if2 ON applicationrole USING btree (application_id);
*/
CREATE SEQUENCE applicationrole_id_gen AS INTEGER START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE applicationrole_id_gen OWNED BY applicationrole.applicationrole_id;

