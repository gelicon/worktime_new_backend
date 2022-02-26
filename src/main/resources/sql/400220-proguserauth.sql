CREATE TABLE proguserauth(
    proguserauth_id integer NOT NULL,
    proguser_id integer NOT NULL,
    proguserauth_datecreate timestamp NOT NULL,
    proguserauth_lastquery timestamp NOT NULL,
    proguserauth_dateend timestamp,
    proguserauth_token varchar(128) NOT NULL,
    CONSTRAINT proguserauth_pk PRIMARY KEY (proguserauth_id),
    CONSTRAINT proguserauth_fk1 FOREIGN KEY (proguser_id)
        REFERENCES proguser (proguser_id) ON UPDATE CASCADE  ON DELETE CASCADE
);
CREATE SEQUENCE proguserauth_id_gen AS INTEGER START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE proguserauth_id_gen OWNED BY proguserauth.proguserauth_id;
