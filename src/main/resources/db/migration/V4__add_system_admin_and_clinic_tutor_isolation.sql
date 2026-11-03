ALTER TABLE tb_usuario DROP CONSTRAINT IF EXISTS ck_tb_usuario_role;

ALTER TABLE tb_usuario ADD CONSTRAINT ck_tb_usuario_role CHECK (
    role IN ('ADMIN_SISTEMA', 'TUTOR', 'ADMIN_CLINICA', 'VETERINARIO')
);

ALTER TABLE tb_usuario DROP CONSTRAINT IF EXISTS ck_tb_usuario_profile_link;

ALTER TABLE tb_usuario ADD CONSTRAINT ck_tb_usuario_profile_link CHECK (
    (role = 'ADMIN_SISTEMA' AND clinic_id IS NULL AND tutor_id IS NULL AND veterinarian_id IS NULL)
    OR (role = 'TUTOR' AND tutor_id IS NOT NULL)
    OR (role = 'ADMIN_CLINICA' AND clinic_id IS NOT NULL)
    OR (role = 'VETERINARIO' AND veterinarian_id IS NOT NULL)
);

CREATE TABLE tb_clinica_tutor (
    id BIGSERIAL PRIMARY KEY,
    clinic_id BIGINT NOT NULL REFERENCES tb_clinica(id),
    tutor_id BIGINT NOT NULL REFERENCES tb_tutor(id),
    CONSTRAINT uk_clinica_tutor UNIQUE (clinic_id, tutor_id)
);

INSERT INTO tb_clinica_tutor (clinic_id, tutor_id) VALUES
(1, 1),
(2, 2);

INSERT INTO tb_usuario (username, password, role, clinic_id, tutor_id, veterinarian_id) VALUES
('admin.sistema@petjourney.com', '$2a$12$DTh/iKaYkvqew6JTHjbmbuiWtgnj/KhzJH1k0utvJYqJFos.8Vste', 'ADMIN_SISTEMA', NULL, NULL, NULL);

