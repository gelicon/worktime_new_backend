CREATE TABLE sqlaction (
	sqlaction_id int4 NOT NULL,
	sqlaction_sql varchar(10) NOT NULL,
	sqlaction_note varchar(20) NULL,
	CONSTRAINT sqlaction_ak1 UNIQUE (sqlaction_sql),
	CONSTRAINT sqlaction_pk PRIMARY KEY (sqlaction_id)
);

INSERT INTO sqlaction (sqlaction_id,sqlaction_sql,sqlaction_note) VALUES (1,'SELECT','Просмотр');
INSERT INTO sqlaction (sqlaction_id,sqlaction_sql,sqlaction_note) VALUES (2,'INSERT','Добавление');
INSERT INTO sqlaction (sqlaction_id,sqlaction_sql,sqlaction_note) VALUES (3,'UPDATE','Изменение');
INSERT INTO sqlaction (sqlaction_id,sqlaction_sql,sqlaction_note) VALUES (4,'DELETE','Удаление');
INSERT INTO sqlaction (sqlaction_id,sqlaction_sql,sqlaction_note) VALUES (5,'SEARCH','Поиск');
INSERT INTO sqlaction (sqlaction_id,sqlaction_sql,sqlaction_note) VALUES (6,'DEBUG','Отладка');
INSERT INTO sqlaction (sqlaction_id,sqlaction_sql,sqlaction_note) VALUES (7,'ABORT','Прерывание');
INSERT INTO sqlaction (sqlaction_id,sqlaction_sql,sqlaction_note) VALUES (8,'CHOWNER','Смена владельца');
INSERT INTO sqlaction (sqlaction_id,sqlaction_sql,sqlaction_note) VALUES (9,'EXECUTE','Выполнение');
INSERT INTO sqlaction (sqlaction_id,sqlaction_sql,sqlaction_note) VALUES (10,'ADMIN','Администрирование');
