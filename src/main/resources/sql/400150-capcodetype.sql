CREATE TABLE capcodetype
(
    capcodetype_id integer NOT NULL,
    capcodetype_code varchar(10) NOT NULL,
    capcodetype_name varchar(50) NOT NULL,
    capcodetype_text bytea,
    CONSTRAINT capcodetype_pk PRIMARY KEY (capcodetype_id),
    CONSTRAINT capcodetype_ak1 UNIQUE (capcodetype_name),
    CONSTRAINT capcodetype_ak2 UNIQUE (capcodetype_code)
);
INSERT INTO capcodetype VALUES (13,'013','Тип статуса пользователя');
INSERT INTO capcodetype VALUES (2148,'2148','Типы аутентификации');
INSERT INTO capcodetype VALUES (4,'004','Тип данных атрибута',NULL);
INSERT INTO capcodetype VALUES (90,'90','Канал оповещения',NULL);
INSERT INTO capcodetype VALUES (99,'99','Периодичность',NULL);
