CREATE TABLE capcode
(
    capcode_id integer NOT NULL,
    capcodetype_id integer NOT NULL,
    capcode_code varchar(10) NOT NULL,
    capcode_name varchar(50) NOT NULL,
    capcode_sortcode varchar(10),
    capcode_text bytea,
    CONSTRAINT capcode_pk PRIMARY KEY (capcode_id),
    CONSTRAINT capcode_ak1 UNIQUE (capcodetype_id, capcode_name),
    CONSTRAINT capcode_ak2 UNIQUE (capcodetype_id, capcode_code),
    CONSTRAINT capcode_fk1 FOREIGN KEY (capcodetype_id)
        REFERENCES capcodetype (capcodetype_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
);

INSERT INTO capcode VALUES (1301, 13,'1301','Активный','001');
INSERT INTO capcode VALUES (1302, 13,'1302','Неактивный','002');
INSERT INTO capcode VALUES (21481, 2148,'1302','Аутентификация по паролю','01');
INSERT INTO capcode VALUES (404,4,'404','Строка','004',NULL);

INSERT INTO capcode VALUES (9001, 90, '9001','Электронная почта', '001', NULL);
INSERT INTO capcode VALUES (9002, 90, '9002','Телефон', '002', NULL);
INSERT INTO capcode VALUES (9003, 90, '9003','Прочий', '003', NULL);

INSERT INTO capcode VALUES (9901, 99, '9901','Ежедневно', '001', NULL);
INSERT INTO capcode VALUES (9902, 99, '9902','Еженедельно', '002', NULL);
INSERT INTO capcode VALUES (9903, 99, '9903','Ежемесячно', '003', NULL);
INSERT INTO capcode VALUES (9904, 99, '9904','Ежегодно', '004', NULL);
INSERT INTO capcode VALUES (9905, 99, '9905','Ежечасно', '005', NULL);
INSERT INTO capcode VALUES (9906, 99, '9906','Ежеминутно', '006', NULL);
