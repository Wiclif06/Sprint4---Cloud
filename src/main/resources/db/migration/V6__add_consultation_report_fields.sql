ALTER TABLE tb_prontuario ADD COLUMN appointment_id BIGINT UNIQUE REFERENCES tb_agendamento(id);
ALTER TABLE tb_prontuario ADD COLUMN clinical_notes VARCHAR(2000);
ALTER TABLE tb_prontuario ADD COLUMN recommendations VARCHAR(2000);
ALTER TABLE tb_prontuario ADD COLUMN prescription_notes VARCHAR(2000);
ALTER TABLE tb_medicamento ADD COLUMN medical_record_id BIGINT REFERENCES tb_prontuario(id);

UPDATE tb_prontuario SET appointment_id = 1 WHERE id = 1;
UPDATE tb_prontuario SET appointment_id = 2 WHERE id = 2;

UPDATE tb_medicamento SET medical_record_id = 1 WHERE id = 1;
UPDATE tb_medicamento SET medical_record_id = 2 WHERE id = 2;

