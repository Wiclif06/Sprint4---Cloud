CREATE TABLE tb_clinica (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    cnpj VARCHAR(20),
    phone VARCHAR(30),
    email VARCHAR(255),
    address VARCHAR(255)
);

CREATE TABLE tb_tutor (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    phone VARCHAR(30),
    email VARCHAR(255)
);

CREATE TABLE tb_pet (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    species VARCHAR(30),
    breed VARCHAR(255),
    sex VARCHAR(20),
    birth_date DATE,
    weight NUMERIC(10, 2),
    tutor_id BIGINT REFERENCES tb_tutor(id)
);

CREATE TABLE tb_veterinario (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    crmv VARCHAR(50),
    phone VARCHAR(30),
    email VARCHAR(255),
    specialty VARCHAR(255),
    clinic_id BIGINT REFERENCES tb_clinica(id)
);

CREATE TABLE tb_agendamento (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    date_time TIMESTAMP NOT NULL,
    status VARCHAR(30),
    pet_id BIGINT REFERENCES tb_pet(id),
    veterinarian_id BIGINT REFERENCES tb_veterinario(id),
    clinic_id BIGINT REFERENCES tb_clinica(id)
);

CREATE TABLE tb_prontuario (
    id BIGSERIAL PRIMARY KEY,
    registration_date TIMESTAMP NOT NULL,
    main_complaint VARCHAR(1000),
    diagnosis VARCHAR(1000),
    conduct VARCHAR(1000),
    observations VARCHAR(1000),
    pet_id BIGINT REFERENCES tb_pet(id),
    veterinarian_id BIGINT REFERENCES tb_veterinario(id)
);

CREATE TABLE tb_medicamento (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    dosage VARCHAR(255),
    frequency VARCHAR(255),
    start_date DATE,
    end_date DATE,
    observations VARCHAR(1000),
    status VARCHAR(30),
    pet_id BIGINT REFERENCES tb_pet(id),
    veterinarian_id BIGINT REFERENCES tb_veterinario(id)
);

