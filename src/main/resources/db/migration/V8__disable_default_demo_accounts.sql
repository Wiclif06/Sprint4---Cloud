UPDATE tb_usuario
SET active = FALSE
WHERE username IN (
    'admin.sistema@petjourney.com',
    'admin.petfeliz@petjourney.com',
    'admin.saudeanimal@petjourney.com',
    'dr.joao@petjourney.com',
    'carlos@petjourney.com',
    'ana@petjourney.com'
)
AND password = '$2a$12$DTh/iKaYkvqew6JTHjbmbuiWtgnj/KhzJH1k0utvJYqJFos.8Vste';
