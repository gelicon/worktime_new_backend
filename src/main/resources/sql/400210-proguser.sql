/* Пользователи */
CREATE TABLE proguser (
    proguser_id integer NOT NULL,
    progusergroup_id integer DEFAULT 1 NOT NULL,
    proguser_status_id integer NOT NULL,
    proguser_type integer,
    proguser_name varchar(50) NOT NULL,
    proguser_fullname varchar(50),
    proguser_webpassword varchar(128),
    CONSTRAINT proguser_pk PRIMARY KEY (proguser_id),
    CONSTRAINT proguser_ak1 UNIQUE (proguser_name)
);
CREATE SEQUENCE proguser_id_gen AS INTEGER START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE proguser_id_gen OWNED BY proguser.proguser_id;

CREATE VIEW ft_proguser
AS
SELECT
    proguser_id,
    CAST(
        trim(proguser_name)||' '||coalesce(proguser_fullname,'')
        AS varchar(268)
        ) AS fulltext
FROM proguser;
