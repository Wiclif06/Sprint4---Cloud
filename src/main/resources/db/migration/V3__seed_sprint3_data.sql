INSERT INTO tb_clinica (name, cnpj, phone, email, address) VALUES
('Clínica Veterinária Pet Feliz', '12345678000190', '11987654321', 'contato@petfeliz.com.br', 'Rua dos Animais, 123 - São Paulo'),
('Hospital Veterinário Saúde Animal', '98765432000110', '11912345678', 'atendimento@saudeanimal.com', 'Av. Paulista, 1000 - São Paulo');

INSERT INTO tb_tutor (name, cpf, phone, email) VALUES
('Carlos Souza', '12345678901', '11911112222', 'carlos.souza@email.com'),
('Ana Paula Lima', '98765432100', '11933334444', 'ana.lima@email.com');

INSERT INTO tb_pet (name, species, breed, sex, birth_date, weight, tutor_id) VALUES
('Rex', 'CACHORRO', 'Golden Retriever', 'MACHO', '2020-05-15', 32.50, 1),
('Mel', 'GATO', 'Persa', 'FEMEA', '2021-08-20', 4.20, 1),
('Thor', 'CACHORRO', 'Bulldog Francês', 'MACHO', '2022-01-10', 12.80, 2);

INSERT INTO tb_veterinario (name, crmv, phone, email, specialty, clinic_id) VALUES
('Dr. João Silva', 'CRMV-SP 12345', '11977776666', 'joao.silva@vet.com', 'Clínico Geral', 1),
('Dra. Maria Oliveira', 'CRMV-SP 54321', '11966665555', 'maria.oliveira@vet.com', 'Cirurgiã', 1),
('Dr. Ricardo Santos', 'CRMV-SP 99887', '11944443333', 'ricardo.santos@vet.com', 'Dermatologista', 2);

INSERT INTO tb_agendamento (title, description, date_time, status, pet_id, veterinarian_id, clinic_id) VALUES
('Consulta preventiva', 'Check-up anual do Rex', '2026-09-10 09:00:00', 'PENDENTE', 1, 1, 1),
('Retorno Otite', 'Avaliação do tratamento de otite', '2026-09-11 16:00:00', 'PENDENTE', 3, 3, 2);

INSERT INTO tb_prontuario (registration_date, main_complaint, diagnosis, conduct, observations, pet_id, veterinarian_id) VALUES
('2026-08-01 10:00:00', 'Vômitos e diarreia', 'Gastroenterite leve', 'Dieta leve e hidratação', 'Animal estável', 1, 1),
('2026-08-15 14:30:00', 'Coceira intensa nas orelhas', 'Otite bacteriana', 'Limpeza e antibiótico local', 'Ouvido direito inflamado', 3, 3);

INSERT INTO tb_medicamento (name, dosage, frequency, start_date, end_date, observations, status, pet_id, veterinarian_id) VALUES
('Amoxicilina', '250mg', '12 em 12 horas', '2026-08-01', '2026-08-08', 'Dar após a refeição', 'FINALIZADO', 1, 1),
('Otopet Gotas', '3 gotas', '8 em 8 horas', '2026-08-15', '2026-08-22', 'Limpar antes de aplicar', 'ATIVO', 3, 3);

INSERT INTO tb_usuario (username, password, role, clinic_id, tutor_id, veterinarian_id) VALUES
('admin.petfeliz@petjourney.com', '$2a$12$DTh/iKaYkvqew6JTHjbmbuiWtgnj/KhzJH1k0utvJYqJFos.8Vste', 'ADMIN_CLINICA', 1, NULL, NULL),
('admin.saudeanimal@petjourney.com', '$2a$12$DTh/iKaYkvqew6JTHjbmbuiWtgnj/KhzJH1k0utvJYqJFos.8Vste', 'ADMIN_CLINICA', 2, NULL, NULL),
('dr.joao@petjourney.com', '$2a$12$DTh/iKaYkvqew6JTHjbmbuiWtgnj/KhzJH1k0utvJYqJFos.8Vste', 'VETERINARIO', NULL, NULL, 1),
('carlos@petjourney.com', '$2a$12$DTh/iKaYkvqew6JTHjbmbuiWtgnj/KhzJH1k0utvJYqJFos.8Vste', 'TUTOR', NULL, 1, NULL),
('ana@petjourney.com', '$2a$12$DTh/iKaYkvqew6JTHjbmbuiWtgnj/KhzJH1k0utvJYqJFos.8Vste', 'TUTOR', NULL, 2, NULL);

