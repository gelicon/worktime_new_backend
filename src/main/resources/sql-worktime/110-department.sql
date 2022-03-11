/* Отдел */
CREATE TABLE department (
  department_id              INTEGER     NOT NULL,
  department_name            VARCHAR(50) NOT NULL,
	department_status          INTEGER DEFAULT 1 NOT NULL,
  CONSTRAINT department_pk  PRIMARY KEY (department_id),
  CONSTRAINT department_ak1 UNIQUE      (department_name)
);
CREATE SEQUENCE department_id_gen;
ALTER SEQUENCE department_id_gen RESTART WITH 1;
INSERT INTO department VALUES(1,'Администрация',1);
INSERT INTO department VALUES(2,'Отдел продаж',1);
INSERT INTO department VALUES(3,'Отдел разработки',1);
INSERT INTO department VALUES(4,'Отдел контроля качества',1);
ALTER SEQUENCE department_id_gen RESTART WITH 4;
