-- Execute no PostgreSQL em nuvem apos cada operacao feita pela API.
-- Substitua os IDs pelos retornados no POST.

SELECT id, name, cnpj, phone, email, address, active
FROM tb_clinica
ORDER BY id;

SELECT id, name, crmv, specialty, clinic_id, active
FROM tb_veterinario
ORDER BY id;

SELECT c.id AS clinic_id, c.name AS clinica,
       v.id AS veterinarian_id, v.name AS veterinario, v.active
FROM tb_clinica c
JOIN tb_veterinario v ON v.clinic_id = c.id
ORDER BY c.id, v.id;

-- DELETE e logico: o registro permanece com active = false.
-- Execute os mesmos SELECTs depois de outro deploy para verificar a persistencia.

