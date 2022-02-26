CREATE TABLE proguserrole (
    proguserrole_id INTEGER NOT NULL,
    proguser_id INTEGER NOT NULL,
    accessrole_id INTEGER NOT NULL,
    CONSTRAINT proguserrole_ak1 UNIQUE (proguser_id, accessrole_id),
    CONSTRAINT proguserrole_pk PRIMARY KEY (proguserrole_id),
    CONSTRAINT proguserrole_fk1 FOREIGN KEY (proguser_id) REFERENCES proguser(proguser_id),
    CONSTRAINT proguserrole_fk2 FOREIGN KEY (accessrole_id) REFERENCES accessrole(accessrole_id)
);
CREATE SEQUENCE proguserrole_id_gen AS INTEGER START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE proguserrole_id_gen OWNED BY proguserrole.proguserrole_id;

/*
CREATE INDEX proguserrole_if1 ON proguserrole USING btree (proguser_id);
CREATE INDEX proguserrole_if2 ON proguserrole USING btree (accessrole_id);
*/
