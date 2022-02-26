/*
Группы пользователей
*/
CREATE TABLE progusergroup(
    progusergroup_id integer NOT NULL,
    progusergroup_name varchar(30) NOT NULL,
    progusergroup_note varchar(255),
    progusergroup_visible integer NOT NULL,
    CONSTRAINT progusergroup_pk PRIMARY KEY (progusergroup_id),
    CONSTRAINT progusergroup_ak1 UNIQUE (progusergroup_name)
);
CREATE SEQUENCE progusergroup_id_gen AS INTEGER START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE progusergroup_id_gen OWNED BY progusergroup.progusergroup_id;

