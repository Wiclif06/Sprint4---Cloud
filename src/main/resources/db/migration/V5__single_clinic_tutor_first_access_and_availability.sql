ALTER TABLE tb_tutor ADD COLUMN clinic_id BIGINT;

UPDATE tb_tutor t
SET clinic_id = (
    SELECT MIN(ct.clinic_id)
    FROM tb_clinica_tutor ct
    WHERE ct.tutor_id = t.id
);

ALTER TABLE tb_tutor ALTER COLUMN clinic_id SET NOT NULL;
ALTER TABLE tb_tutor ADD CONSTRAINT fk_tb_tutor_clinica FOREIGN KEY (clinic_id) REFERENCES tb_clinica(id);

ALTER TABLE tb_usuario ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE tb_usuario ADD COLUMN first_access_code VARCHAR(20);
ALTER TABLE tb_usuario ADD COLUMN first_access_expires_at TIMESTAMP;
ALTER TABLE tb_usuario ADD COLUMN first_access_used_at TIMESTAMP;

CREATE TABLE tb_disponibilidade_veterinario (
    id BIGSERIAL PRIMARY KEY,
    veterinarian_id BIGINT NOT NULL REFERENCES tb_veterinario(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    CONSTRAINT ck_disponibilidade_periodo CHECK (end_time > start_time),
    CONSTRAINT uk_disponibilidade_vet_inicio UNIQUE (veterinarian_id, start_time)
);

INSERT INTO tb_disponibilidade_veterinario (veterinarian_id, start_time, end_time) VALUES
(1, '2026-09-20 10:00:00', '2026-09-20 10:30:00'),
(1, '2026-09-20 10:30:00', '2026-09-20 11:00:00'),
(2, '2026-09-20 14:00:00', '2026-09-20 14:30:00'),
(3, '2026-09-21 09:00:00', '2026-09-21 09:30:00');

