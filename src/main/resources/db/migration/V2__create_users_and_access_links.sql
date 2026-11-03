CREATE TABLE tb_usuario (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    clinic_id BIGINT REFERENCES tb_clinica(id),
    tutor_id BIGINT UNIQUE REFERENCES tb_tutor(id),
    veterinarian_id BIGINT UNIQUE REFERENCES tb_veterinario(id),
    CONSTRAINT ck_tb_usuario_role CHECK (role IN ('TUTOR', 'ADMIN_CLINICA', 'VETERINARIO')),
    CONSTRAINT ck_tb_usuario_profile_link CHECK (
        (role = 'TUTOR' AND tutor_id IS NOT NULL)
        OR (role = 'ADMIN_CLINICA' AND clinic_id IS NOT NULL)
        OR (role = 'VETERINARIO' AND veterinarian_id IS NOT NULL)
    )
);

