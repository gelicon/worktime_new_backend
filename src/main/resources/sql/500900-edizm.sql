CREATE TABLE edizm (
    edizm_id INTEGER NOT NULL,
    edizm_name varchar(50),
    edizm_notation varchar(15) NOT NULL,
    edizm_blockflag integer NOT NULL,
    edizm_code varchar(20) NOT NULL,
    CONSTRAINT edizm_pk PRIMARY KEY (edizm_id),
    CONSTRAINT edizm_ak1 UNIQUE (edizm_notation),
    CONSTRAINT edizm_ak2 UNIQUE (edizm_code)
);
CREATE SEQUENCE edizm_id_gen AS INTEGER START WITH 1 INCREMENT BY 1;
ALTER SEQUENCE edizm_id_gen OWNED BY edizm.edizm_id;
COMMENT ON TABLE edizm IS 'Единицы измерения';
COMMENT ON COLUMN edizm.edizm_id IS 'Единицы измерения ИД';
COMMENT ON COLUMN edizm.edizm_name IS 'Наименование единицы измерения';
COMMENT ON COLUMN edizm.edizm_notation IS 'Краткое наименование единицы измерения';
COMMENT ON COLUMN edizm.edizm_code IS 'Код единицы измерения';

CREATE VIEW ft_edizm
AS
SELECT
	edizm_id,
	CAST(
		trim(edizm_code)||' '||trim(edizm_notation)||' '||coalesce(edizm_name,'')
		AS varchar(100)
	) as fulltext
FROM edizm;
