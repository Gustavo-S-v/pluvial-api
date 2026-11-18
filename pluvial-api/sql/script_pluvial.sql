-- Pluvial - Monitoramento de enchentes (Global Solution 2026/2)
-- Relacionamentos 1:N: tb_regiao -> tb_sensor -> tb_leitura
-- Rodar como script (F5 no SQL Developer).

-- 1. LIMPEZA
BEGIN
    FOR t IN (SELECT table_name FROM user_tables WHERE table_name IN ('TB_LEITURA','TB_SENSOR','TB_REGIAO')) LOOP
        EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS';
    END LOOP;
    FOR s IN (SELECT sequence_name FROM user_sequences WHERE sequence_name IN ('SQ_TB_REGIAO','SQ_TB_SENSOR','SQ_TB_LEITURA')) LOOP
        EXECUTE IMMEDIATE 'DROP SEQUENCE ' || s.sequence_name;
    END LOOP;
END;
/

-- 2. SEQUENCES
CREATE SEQUENCE sq_tb_regiao START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_sensor START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE sq_tb_leitura START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- 3. TABELAS
CREATE TABLE tb_regiao (cd_regiao NUMBER(10) NOT NULL, nm_regiao VARCHAR2(100) NOT NULL, nm_cidade VARCHAR2(100) NOT NULL, ds_nivel_risco VARCHAR2(20) DEFAULT 'NORMAL' NOT NULL, dt_cadastro DATE DEFAULT SYSDATE NOT NULL);
CREATE TABLE tb_sensor (cd_sensor NUMBER(10) NOT NULL, cd_regiao NUMBER(10) NOT NULL, nm_sensor VARCHAR2(100) NOT NULL, ds_tipo VARCHAR2(20) NOT NULL, ds_localizacao VARCHAR2(200) NOT NULL, vl_limite_alerta NUMBER(8,2) NOT NULL, ds_status VARCHAR2(20) DEFAULT 'ATIVO' NOT NULL, dt_instalacao DATE DEFAULT SYSDATE NOT NULL);
CREATE TABLE tb_leitura (cd_leitura NUMBER(10) NOT NULL, cd_sensor NUMBER(10) NOT NULL, vl_leitura NUMBER(8,2) NOT NULL, dt_leitura DATE DEFAULT SYSDATE NOT NULL);

-- 4. CONSTRAINTS
ALTER TABLE tb_regiao ADD CONSTRAINT pk_regiao PRIMARY KEY (cd_regiao);
ALTER TABLE tb_regiao ADD CONSTRAINT uk_regiao_nome_cidade UNIQUE (nm_regiao, nm_cidade);
ALTER TABLE tb_regiao ADD CONSTRAINT ck_regiao_nivel CHECK (ds_nivel_risco IN ('NORMAL','ATENCAO','ALERTA','EMERGENCIA'));
ALTER TABLE tb_sensor ADD CONSTRAINT pk_sensor PRIMARY KEY (cd_sensor);
ALTER TABLE tb_sensor ADD CONSTRAINT fk_sensor_regiao FOREIGN KEY (cd_regiao) REFERENCES tb_regiao (cd_regiao);
ALTER TABLE tb_sensor ADD CONSTRAINT ck_sensor_tipo CHECK (ds_tipo IN ('PLUVIOMETRO','NIVEL_RIO'));
ALTER TABLE tb_sensor ADD CONSTRAINT ck_sensor_status CHECK (ds_status IN ('ATIVO','INATIVO'));
ALTER TABLE tb_sensor ADD CONSTRAINT ck_sensor_limite CHECK (vl_limite_alerta > 0);
ALTER TABLE tb_leitura ADD CONSTRAINT pk_leitura PRIMARY KEY (cd_leitura);
ALTER TABLE tb_leitura ADD CONSTRAINT fk_leitura_sensor FOREIGN KEY (cd_sensor) REFERENCES tb_sensor (cd_sensor);
ALTER TABLE tb_leitura ADD CONSTRAINT ck_leitura_valor CHECK (vl_leitura >= 0);

-- 5. INDICES
CREATE INDEX idx_sensor_regiao ON tb_sensor (cd_regiao);
CREATE INDEX idx_leitura_sensor ON tb_leitura (cd_sensor);

-- 6. DADOS DE EXEMPLO
INSERT INTO tb_regiao (cd_regiao, nm_regiao, nm_cidade, ds_nivel_risco) VALUES (sq_tb_regiao.NEXTVAL, 'Marginal Tiete', 'Sao Paulo', 'NORMAL');
INSERT INTO tb_regiao (cd_regiao, nm_regiao, nm_cidade, ds_nivel_risco) VALUES (sq_tb_regiao.NEXTVAL, 'Centro', 'Blumenau', 'ALERTA');
INSERT INTO tb_sensor (cd_sensor, cd_regiao, nm_sensor, ds_tipo, ds_localizacao, vl_limite_alerta, ds_status) VALUES (sq_tb_sensor.NEXTVAL, (SELECT cd_regiao FROM tb_regiao WHERE nm_regiao = 'Marginal Tiete'), 'Pluviometro Tiete 01', 'PLUVIOMETRO', 'Ponte das Bandeiras', 50, 'ATIVO');
INSERT INTO tb_sensor (cd_sensor, cd_regiao, nm_sensor, ds_tipo, ds_localizacao, vl_limite_alerta, ds_status) VALUES (sq_tb_sensor.NEXTVAL, (SELECT cd_regiao FROM tb_regiao WHERE nm_regiao = 'Marginal Tiete'), 'Nivel Rio Tiete 01', 'NIVEL_RIO', 'Ponte da Casa Verde', 300, 'ATIVO');
INSERT INTO tb_sensor (cd_sensor, cd_regiao, nm_sensor, ds_tipo, ds_localizacao, vl_limite_alerta, ds_status) VALUES (sq_tb_sensor.NEXTVAL, (SELECT cd_regiao FROM tb_regiao WHERE nm_regiao = 'Centro'), 'Pluviometro Centro 01', 'PLUVIOMETRO', 'Praca Central', 60, 'ATIVO');
INSERT INTO tb_sensor (cd_sensor, cd_regiao, nm_sensor, ds_tipo, ds_localizacao, vl_limite_alerta, ds_status) VALUES (sq_tb_sensor.NEXTVAL, (SELECT cd_regiao FROM tb_regiao WHERE nm_regiao = 'Centro'), 'Pluviometro Centro 02', 'PLUVIOMETRO', 'Rua XV de Novembro', 60, 'INATIVO');
INSERT INTO tb_leitura (cd_leitura, cd_sensor, vl_leitura) VALUES (sq_tb_leitura.NEXTVAL, (SELECT cd_sensor FROM tb_sensor WHERE nm_sensor = 'Pluviometro Tiete 01'), 10.5);
INSERT INTO tb_leitura (cd_leitura, cd_sensor, vl_leitura) VALUES (sq_tb_leitura.NEXTVAL, (SELECT cd_sensor FROM tb_sensor WHERE nm_sensor = 'Pluviometro Tiete 01'), 18);
INSERT INTO tb_leitura (cd_leitura, cd_sensor, vl_leitura) VALUES (sq_tb_leitura.NEXTVAL, (SELECT cd_sensor FROM tb_sensor WHERE nm_sensor = 'Nivel Rio Tiete 01'), 120);
INSERT INTO tb_leitura (cd_leitura, cd_sensor, vl_leitura) VALUES (sq_tb_leitura.NEXTVAL, (SELECT cd_sensor FROM tb_sensor WHERE nm_sensor = 'Pluviometro Centro 01'), 54);
COMMIT;

-- 7. CONFERENCIA
SELECT r.nm_regiao, r.ds_nivel_risco, s.nm_sensor, s.ds_status, COUNT(l.cd_leitura) AS qt_leituras FROM tb_regiao r JOIN tb_sensor s ON s.cd_regiao = r.cd_regiao LEFT JOIN tb_leitura l ON l.cd_sensor = s.cd_sensor GROUP BY r.nm_regiao, r.ds_nivel_risco, s.nm_sensor, s.ds_status ORDER BY r.nm_regiao, s.nm_sensor;
