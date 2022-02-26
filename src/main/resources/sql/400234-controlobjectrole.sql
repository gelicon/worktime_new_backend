CREATE TABLE controlobjectrole (
    controlobjectrole_id int4 NOT NULL,
    controlobject_id int4 NOT NULL,
    accessrole_id int4 NOT NULL,
    sqlaction_id int4 NOT NULL,
    CONSTRAINT controlobjectrole_ak1 UNIQUE (controlobject_id, accessrole_id, sqlaction_id),
    CONSTRAINT controlobjectrole_pk PRIMARY KEY (controlobjectrole_id),
    CONSTRAINT controlobjectrole_fk1 FOREIGN KEY (controlobject_id) REFERENCES controlobject(controlobject_id),
    CONSTRAINT controlobjectrole_fk2 FOREIGN KEY (accessrole_id) REFERENCES accessrole(accessrole_id),
    CONSTRAINT controlobjectrole_fk3 FOREIGN KEY (sqlaction_id) REFERENCES sqlaction(sqlaction_id)
);
/*
CREATE INDEX controlobjectrole_if1 ON controlobjectrole USING btree (controlobject_id);
CREATE INDEX controlobjectrole_if2 ON controlobjectrole USING btree (accessrole_id);
*/
CREATE SEQUENCE controlobjectrole_id_gen AS INTEGER START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE controlobjectrole_id_gen OWNED BY controlobjectrole.controlobjectrole_id;
