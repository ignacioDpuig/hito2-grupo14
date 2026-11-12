
-- 1. Insertar Roles
INSERT INTO rol (nombre) VALUES ('ROLE_ADMIN');
INSERT INTO rol (nombre) VALUES ('ROLE_RESPONSABLE');
INSERT INTO rol (nombre) VALUES ('ROLE_EMPLEADO');

SET @admin_role = (SELECT id FROM rol WHERE nombre = 'ROLE_ADMIN');
SET @responsable_role = (SELECT id FROM rol WHERE nombre = 'ROLE_RESPONSABLE');
SET @empleado_role = (SELECT id FROM rol WHERE nombre = 'ROLE_EMPLEADO');

-- 2. Insertar Administrador (pass: admin)
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo)
VALUES ('Admin', 'Sistema', 99999999, '2000-01-01', '2024-01-01', 'admin@admin',
        '$2a$10$n9JqulIWq/h.BBVGKAbedO2wvwdhwgCe8YShW/.APLEWp9fbsrm9O', true);
SET @admin_id = LAST_INSERT_ID();
INSERT INTO administrador (id) VALUES (@admin_id);
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@admin_id, @admin_role);

-- 3. Insertar Festival
INSERT INTO festival (nombre, temporada, fecha_inicio, fecha_fin)
VALUES ('Festival Gastronómico 2024', 'Verano', '2024-12-15', '2025-01-21');
SET @festival_id = LAST_INSERT_ID();

-- 4. Insertar Responsable 1 (pass: pass123)
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo)
VALUES ('Juan', 'García', 12345678, '1990-05-15', '2023-01-10', 'juan@email.com',
        '$2a$10$3JFIechuk505S3KfYyv.A.G2ABLfI9JAKZRlkFJmc3VaS8OIEX/02', true);
SET @responsable1_id = LAST_INSERT_ID();
INSERT INTO responsable (id) VALUES (@responsable1_id);
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@responsable1_id, @responsable_role);
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@responsable1_id, @empleado_role);

-- 5. Insertar Cocinero para Unidad 1 (pass: pass456)
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo)
VALUES ('Carlos', 'López', 87654321, '1988-03-20', '2023-02-15', 'carlos@email.com',
        '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @cocinero1_id = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@cocinero1_id);
INSERT INTO cocinero (id, especialidad, categoria) VALUES (@cocinero1_id, 'Pizzas', 'COCINERO');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@cocinero1_id, @empleado_role);

-- 6. Insertar Responsable 2 (pass: responsable)
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo)
VALUES ('María', 'Rodríguez', 11111111, '1992-07-22', '2023-03-05', 'responsable@responsable',
        '$2a$10$9omk.7usAUe5H8q7t.ee0.I3MxD8gZ8uTDLNBdpkLryeD/ggV1rYC', true);
SET @responsable2_id = LAST_INSERT_ID();
INSERT INTO responsable (id) VALUES (@responsable2_id);
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@responsable2_id, @responsable_role);
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@responsable2_id, @empleado_role);

-- 7. Insertar Cocinero para Unidad 2 (pass: empleado)
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo)
VALUES ('Alberto', 'Martínez', 22222222, '1989-11-10', '2023-04-12', 'empleado@empleado',
        '$2a$10$BtRyWjt5wNhnhGMVVVB4buZHIy4bEGsxIIcsdEOh2gJTrtb9T3g32', true);
SET @cocinero2_id = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@cocinero2_id);
INSERT INTO cocinero (id, especialidad, categoria) VALUES (@cocinero2_id, 'Parrillas', 'LAVAPLATOS');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@cocinero2_id, @empleado_role);

-- 8. Insertar Unidad de Venta 1 (FoodTruck)
INSERT INTO unidad_venta (nombre_comercial, codigo, superficie, responsable_id, festival_id)
VALUES ('Food Truck Sabores del Sur', 'FTSDELSUR1', 25.5, @responsable1_id, @festival_id);
SET @unidad1_id = LAST_INSERT_ID();
INSERT INTO food_truck (id, patente, esta_conectado) VALUES (@unidad1_id, 'ABC123', true);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad1_id, @responsable1_id);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad1_id, @cocinero1_id);

-- 9. Insertar Unidad de Venta 2 (PuestoDesarmable)
INSERT INTO unidad_venta (nombre_comercial, codigo, superficie, responsable_id, festival_id)
VALUES ('Puesto Desarmable La Parrilla', 'PDPARRLL01', 30.0, @responsable2_id, @festival_id);
SET @unidad2_id = LAST_INSERT_ID();
INSERT INTO puesto_desarmable (id, cantidad_carpas, tiempo_montaje) VALUES (@unidad2_id, 3, 2.5);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad2_id, @responsable2_id);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad2_id, @cocinero2_id);

-- 10. Insertar Platos para Unidad 1 (precios y cantidades pensados para que la rentabilidad neta sea positiva)
INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Pizza Margherita', 3000.00, 10000.00, @unidad1_id);
SET @plato1_1 = LAST_INSERT_ID();
INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Pizza Pepperoni', 3600.00, 12000.00, @unidad1_id);
SET @plato1_2 = LAST_INSERT_ID();
INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Pizza Vegetariana', 3200.00, 11000.00, @unidad1_id);
SET @plato1_3 = LAST_INSERT_ID();

-- 11. Insertar Platos para Unidad 2
INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Asado Argentino', 5000.00, 16000.00, @unidad2_id);
SET @plato2_1 = LAST_INSERT_ID();
INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Costillas BBQ', 4400.00, 15000.00, @unidad2_id);
SET @plato2_2 = LAST_INSERT_ID();
INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Choripán', 2400.00, 8000.00, @unidad2_id);
SET @plato2_3 = LAST_INSERT_ID();

-- 12. Insertar Pedidos para Unidad 1
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES ('2024-12-20', @festival_id, @unidad1_id);
SET @pedido1_1 = LAST_INSERT_ID();
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES ('2024-12-22', @festival_id, @unidad1_id);
SET @pedido1_2 = LAST_INSERT_ID();

-- 13. Insertar Pedidos para Unidad 2
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES ('2024-12-21', @festival_id, @unidad2_id);
SET @pedido2_1 = LAST_INSERT_ID();
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES ('2024-12-23', @festival_id, @unidad2_id);
SET @pedido2_2 = LAST_INSERT_ID();

-- 14. Insertar Detalles de Pedidos Unidad 1
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido1_1, @plato1_1, 50, 10000.00, 3000.00);
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido1_1, @plato1_2, 30, 12000.00, 3600.00);
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido1_2, @plato1_3, 40, 11000.00, 3200.00);
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido1_2, @plato1_1, 20, 10000.00, 3000.00);

-- 15. Insertar Detalles de Pedidos Unidad 2
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido2_1, @plato2_1, 20, 16000.00, 5000.00);
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido2_1, @plato2_2, 30, 15000.00, 4400.00);
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido2_2, @plato2_3, 60, 8000.00, 2400.00);
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido2_2, @plato2_1, 10, 16000.00, 5000.00);

-- 16. Configuración de Costos
INSERT INTO configuracion_costos (costo_superficie, costo_montaje, plus_electricidad, sueldo_base, plus_cocinero, plus_cocinero_ayudante, plus_lavaplatos, monto_antiguedad, festival_id)
VALUES (500, 10, 2000, 100000, 150000, 100000, 50000, 5000, @festival_id);

-- 17. Festival en curso para la defensa. Las fechas son relativas al día en que arranca la app,
--     así "festival actual" y "cerrar caja" se pueden probar cualquier día.
INSERT INTO festival (nombre, temporada, fecha_inicio, fecha_fin)
VALUES ('Festival Primavera 2026', 'Primavera', CURRENT_DATE - INTERVAL '10' DAY, CURRENT_DATE + INTERVAL '30' DAY);
SET @festival2_id = LAST_INSERT_ID();

INSERT INTO configuracion_costos (costo_superficie, costo_montaje, plus_electricidad, sueldo_base, plus_cocinero, plus_cocinero_ayudante, plus_lavaplatos, monto_antiguedad, festival_id)
VALUES (500, 10, 2000, 100000, 150000, 100000, 50000, 5000, @festival2_id);

-- 18. Responsable 3 (pass: sofia123)
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo)
VALUES ('Sofía', 'Benítez', 30111222, '1991-02-10', '2024-03-01', 'sofia@email.com',
        '$2a$10$kogazez6qXq.Oipr6wWiaeUKme5.F5xzBrWzWwOjzmKOTbimpoMZO', true);
SET @responsable3_id = LAST_INSERT_ID();
INSERT INTO responsable (id) VALUES (@responsable3_id);
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@responsable3_id, @responsable_role);
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@responsable3_id, @empleado_role);

-- 19. Cajero (pass: cajero)
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo)
VALUES ('Lucas', 'Fernández', 40111222, '1998-06-01', '2023-10-01', 'cajero@cajero',
        '$2a$10$vyeLTFBjF7ZcE2hPGvu0PuMCbc1NZzWcbcKVut/xn2PiE6JXSiRkC', true);
SET @cajero1_id = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@cajero1_id);
INSERT INTO cajero (id, turno) VALUES (@cajero1_id, 'Noche');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@cajero1_id, @empleado_role);

-- 20. Unidad de Venta 3 (FoodTruck) en el festival en curso
INSERT INTO unidad_venta (nombre_comercial, codigo, superficie, responsable_id, festival_id)
VALUES ('Food Truck La Primavera', 'FTPRIMAV26', 20.0, @responsable3_id, @festival2_id);
SET @unidad3_id = LAST_INSERT_ID();
INSERT INTO food_truck (id, patente, esta_conectado) VALUES (@unidad3_id, 'AB123CD', false);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad3_id, @responsable3_id);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad3_id, @cajero1_id);

INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Empanada de Carne', 800.00, 2500.00, @unidad3_id);
SET @plato3_1 = LAST_INSERT_ID();
INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Hamburguesa Completa', 3000.00, 9000.00, @unidad3_id);
SET @plato3_2 = LAST_INSERT_ID();

-- 21. Pedido de hoy, para que el cierre de caja tenga datos: 12 × $2.500 + 3 × $9.000 = $57.000
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES (CURRENT_DATE, @festival2_id, @unidad3_id);
SET @pedido3_1 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido3_1, @plato3_1, 12, 2500.00, 800.00);
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido3_1, @plato3_2, 3, 9000.00, 3000.00);
-- Ventas de exhibición de un día anterior: mantiene intacto el cierre de caja de hoy y cubre los sueldos.
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id)
VALUES (CURRENT_DATE - INTERVAL '1' DAY, @festival2_id, @unidad3_id);
SET @pedido3_2 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido3_2, @plato3_1, 200, 2500.00, 800.00);
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido3_2, @plato3_2, 100, 9000.00, 3000.00);

-- 22. Costos propios de los Food Trucks en Primavera 2026: le ganan a la configuración general (sección 17).
--     Food Truck La Primavera: canon 20 m² × $700 = $14.000; su cajero cobra con sueldo base $110.000
INSERT INTO configuracion_costos (costo_superficie, costo_montaje, plus_electricidad, sueldo_base, plus_cocinero, plus_cocinero_ayudante, plus_lavaplatos, monto_antiguedad, tipo_unidad, festival_id)
VALUES (700, 10, 3000, 110000, 150000, 100000, 50000, 5000, 'FOOD_TRUCK', @festival2_id);

-- 23. Cocinero ayudante que trabajó en dos festivales (pass: martin123):
--     en 2024 en el Puesto La Parrilla y ahora en el Food Truck La Primavera. Tiene historial y dos recibos
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo)
VALUES ('Martín', 'Gómez', 35222333, '1995-08-14', '2022-06-01', 'martin@email.com',
        '$2a$10$8sY554r5ZQFgYtgjpzsFbeOpAfKU6nngYKCyd5vk.S2ZiwesjolWW', true);
SET @ayudante_id = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@ayudante_id);
INSERT INTO cocinero (id, especialidad, categoria) VALUES (@ayudante_id, 'Pastas', 'COCINERO_AYUDANTE');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@ayudante_id, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad2_id, @ayudante_id);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad3_id, @ayudante_id);

-- 24. Cajera dada de baja en el Puesto La Parrilla, su única unidad: no puede iniciar sesión (pass: rocio123).
--     Aparece con "incluir dados de baja" y se puede reactivar, o volver a dar de alta con su DNI
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo)
VALUES ('Rocío', 'Díaz', 38444555, '1997-04-02', '2024-02-01', 'rocio@email.com',
        '$2a$10$239sElpCZKl3WCeUI/rREOc1KbEvTTps.i/x0p6PStQUQ21J.0ILq', true);
SET @cajera_baja_id = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@cajera_baja_id);
INSERT INTO cajero (id, turno) VALUES (@cajera_baja_id, 'Mañana');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@cajera_baja_id, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id, eliminado) VALUES (@unidad2_id, @cajera_baja_id, true);

-- 25. Plato dado de baja: no se ofrece en pedidos nuevos y se puede reactivar
INSERT INTO plato (nombre, costo_produccion, precio_venta, eliminado, unidad_venta_id) VALUES ('Provoleta', 1800.00, 6000.00, true, @unidad2_id);

-- 26. Festival histórico de Otoño, con su unidad original y ventas registradas.
INSERT INTO festival (nombre, temporada, fecha_inicio, fecha_fin)
VALUES ('Festival de Otoño', 'Otoño', '2025-04-01', '2025-04-16');
SET @festival3_id = LAST_INSERT_ID();
INSERT INTO configuracion_costos (costo_superficie, costo_montaje, plus_electricidad, sueldo_base, plus_cocinero, plus_cocinero_ayudante, plus_lavaplatos, monto_antiguedad, festival_id)
VALUES (500, 10, 2000, 100000, 150000, 100000, 50000, 5000, @festival3_id);

INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo)
VALUES ('Pablo', 'Ruiz', 29333444, '1985-09-30', '2024-01-01', 'pablo@email.com',
        '$2a$10$blnU0kYkFmroJhZCr89Rwe6GzP80eV51j3xovvbaHDVyH/6Qxvpse', true);
SET @responsable4_id = LAST_INSERT_ID();
INSERT INTO responsable (id) VALUES (@responsable4_id);
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@responsable4_id, @responsable_role);
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@responsable4_id, @empleado_role);

INSERT INTO unidad_venta (nombre_comercial, codigo, superficie, responsable_id, festival_id)
VALUES ('Puesto Sabores Andinos', 'PDANDINO01', 15.0, @responsable4_id, @festival3_id);
SET @unidad4_id = LAST_INSERT_ID();
INSERT INTO puesto_desarmable (id, cantidad_carpas, tiempo_montaje) VALUES (@unidad4_id, 2, 45);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad4_id, @responsable4_id);
INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Empanadas Andinas', 1000, 5000, @unidad4_id);
SET @plato4_1 = LAST_INSERT_ID();
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id)
VALUES ('2025-04-10', @festival3_id, @unidad4_id);
SET @pedido4_1 = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario)
VALUES (@pedido4_1, @plato4_1, 120, 5000, 1000);

-- 27. Unidades de ejemplo: cada festival queda con cinco unidades y ventas de margen alto.
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES
('Laura', 'Pérez', 51000101, '1990-01-10', '2024-01-01', 'demo-2024-01@festival.local', '$2a$10$3JFIechuk505S3KfYyv.A.G2ABLfI9JAKZRlkFJmc3VaS8OIEX/02', true),
('Diego', 'Suárez', 51000102, '1991-02-11', '2024-01-01', 'demo-2024-02@festival.local', '$2a$10$3JFIechuk505S3KfYyv.A.G2ABLfI9JAKZRlkFJmc3VaS8OIEX/02', true),
('Micaela', 'Torres', 51000103, '1992-03-12', '2024-01-01', 'demo-2024-03@festival.local', '$2a$10$3JFIechuk505S3KfYyv.A.G2ABLfI9JAKZRlkFJmc3VaS8OIEX/02', true),
('Nicolás', 'Díaz', 51000104, '1990-04-13', '2024-01-01', 'demo-2026-01@festival.local', '$2a$10$3JFIechuk505S3KfYyv.A.G2ABLfI9JAKZRlkFJmc3VaS8OIEX/02', true),
('Valentina', 'Luna', 51000105, '1991-05-14', '2024-01-01', 'demo-2026-02@festival.local', '$2a$10$3JFIechuk505S3KfYyv.A.G2ABLfI9JAKZRlkFJmc3VaS8OIEX/02', true),
('Tomás', 'Rojas', 51000106, '1992-06-15', '2024-01-01', 'demo-2026-03@festival.local', '$2a$10$3JFIechuk505S3KfYyv.A.G2ABLfI9JAKZRlkFJmc3VaS8OIEX/02', true),
('Camila', 'Vega', 51000107, '1990-07-16', '2024-01-01', 'demo-2026-04@festival.local', '$2a$10$3JFIechuk505S3KfYyv.A.G2ABLfI9JAKZRlkFJmc3VaS8OIEX/02', true),
('Bruno', 'Molina', 51000108, '1990-08-17', '2024-01-01', 'demo-otono-01@festival.local', '$2a$10$3JFIechuk505S3KfYyv.A.G2ABLfI9JAKZRlkFJmc3VaS8OIEX/02', true),
('Julieta', 'Castro', 51000109, '1991-09-18', '2024-01-01', 'demo-otono-02@festival.local', '$2a$10$3JFIechuk505S3KfYyv.A.G2ABLfI9JAKZRlkFJmc3VaS8OIEX/02', true),
('Facundo', 'Herrera', 51000110, '1992-10-19', '2024-01-01', 'demo-otono-03@festival.local', '$2a$10$3JFIechuk505S3KfYyv.A.G2ABLfI9JAKZRlkFJmc3VaS8OIEX/02', true),
('Abril', 'Morales', 51000111, '1990-11-20', '2024-01-01', 'demo-otono-04@festival.local', '$2a$10$3JFIechuk505S3KfYyv.A.G2ABLfI9JAKZRlkFJmc3VaS8OIEX/02', true);

INSERT INTO responsable (id) SELECT id FROM personal WHERE email IN (
    'demo-2024-01@festival.local', 'demo-2024-02@festival.local', 'demo-2024-03@festival.local',
    'demo-2026-01@festival.local', 'demo-2026-02@festival.local', 'demo-2026-03@festival.local',
    'demo-2026-04@festival.local', 'demo-otono-01@festival.local', 'demo-otono-02@festival.local',
    'demo-otono-03@festival.local', 'demo-otono-04@festival.local'
);
INSERT INTO personal_rol (personal_id, rol_id)
SELECT id, @responsable_role FROM personal WHERE email IN (
    'demo-2024-01@festival.local', 'demo-2024-02@festival.local', 'demo-2024-03@festival.local',
    'demo-2026-01@festival.local', 'demo-2026-02@festival.local', 'demo-2026-03@festival.local',
    'demo-2026-04@festival.local', 'demo-otono-01@festival.local', 'demo-otono-02@festival.local',
    'demo-otono-03@festival.local', 'demo-otono-04@festival.local'
);
INSERT INTO personal_rol (personal_id, rol_id)
SELECT id, @empleado_role FROM personal WHERE email IN (
    'demo-2024-01@festival.local', 'demo-2024-02@festival.local', 'demo-2024-03@festival.local',
    'demo-2026-01@festival.local', 'demo-2026-02@festival.local', 'demo-2026-03@festival.local',
    'demo-2026-04@festival.local', 'demo-otono-01@festival.local', 'demo-otono-02@festival.local',
    'demo-otono-03@festival.local', 'demo-otono-04@festival.local'
);

-- Festival Gastronómico 2024: dos unidades originales + tres nuevas.
INSERT INTO unidad_venta (nombre_comercial, codigo, superficie, responsable_id, festival_id)
VALUES ('Food Truck La Costa', 'FTDELSUR02', 10.0, (SELECT id FROM personal WHERE email = 'demo-2024-01@festival.local'), @festival_id);
SET @unidad5_id = LAST_INSERT_ID();
INSERT INTO food_truck (id, patente, esta_conectado) VALUES (@unidad5_id, 'DEF101', false);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad5_id, (SELECT id FROM personal WHERE email = 'demo-2024-01@festival.local'));

INSERT INTO unidad_venta (nombre_comercial, codigo, superficie, responsable_id, festival_id)
VALUES ('Puesto El Fogón', 'PDGRILL002', 12.0, (SELECT id FROM personal WHERE email = 'demo-2024-02@festival.local'), @festival_id);
SET @unidad6_id = LAST_INSERT_ID();
INSERT INTO puesto_desarmable (id, cantidad_carpas, tiempo_montaje) VALUES (@unidad6_id, 1, 20);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad6_id, (SELECT id FROM personal WHERE email = 'demo-2024-02@festival.local'));

INSERT INTO unidad_venta (nombre_comercial, codigo, superficie, responsable_id, festival_id)
VALUES ('Food Truck El Delta', 'FTDELTA001', 10.0, (SELECT id FROM personal WHERE email = 'demo-2024-03@festival.local'), @festival_id);
SET @unidad7_id = LAST_INSERT_ID();
INSERT INTO food_truck (id, patente, esta_conectado) VALUES (@unidad7_id, 'DEF102', false);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad7_id, (SELECT id FROM personal WHERE email = 'demo-2024-03@festival.local'));

-- Festival Primavera 2026: una unidad original + cuatro nuevas.
INSERT INTO unidad_venta (nombre_comercial, codigo, superficie, responsable_id, festival_id)
VALUES ('Food Truck El Bosque', 'FTVERANO02', 10.0, (SELECT id FROM personal WHERE email = 'demo-2026-01@festival.local'), @festival2_id);
SET @unidad8_id = LAST_INSERT_ID();
INSERT INTO food_truck (id, patente, esta_conectado) VALUES (@unidad8_id, 'DEF103', false);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad8_id, (SELECT id FROM personal WHERE email = 'demo-2026-01@festival.local'));

INSERT INTO unidad_venta (nombre_comercial, codigo, superficie, responsable_id, festival_id)
VALUES ('Puesto Primavera Norte', 'PDPRIMA002', 12.0, (SELECT id FROM personal WHERE email = 'demo-2026-02@festival.local'), @festival2_id);
SET @unidad9_id = LAST_INSERT_ID();
INSERT INTO puesto_desarmable (id, cantidad_carpas, tiempo_montaje) VALUES (@unidad9_id, 1, 20);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad9_id, (SELECT id FROM personal WHERE email = 'demo-2026-02@festival.local'));

INSERT INTO unidad_venta (nombre_comercial, codigo, superficie, responsable_id, festival_id)
VALUES ('Food Truck Primavera Sur', 'FTPRIMAV03', 10.0, (SELECT id FROM personal WHERE email = 'demo-2026-03@festival.local'), @festival2_id);
SET @unidad10_id = LAST_INSERT_ID();
INSERT INTO food_truck (id, patente, esta_conectado) VALUES (@unidad10_id, 'DEF104', false);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad10_id, (SELECT id FROM personal WHERE email = 'demo-2026-03@festival.local'));

INSERT INTO unidad_venta (nombre_comercial, codigo, superficie, responsable_id, festival_id)
VALUES ('Puesto Primavera Oeste', 'PDPRIMAV03', 12.0, (SELECT id FROM personal WHERE email = 'demo-2026-04@festival.local'), @festival2_id);
SET @unidad11_id = LAST_INSERT_ID();
INSERT INTO puesto_desarmable (id, cantidad_carpas, tiempo_montaje) VALUES (@unidad11_id, 1, 20);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad11_id, (SELECT id FROM personal WHERE email = 'demo-2026-04@festival.local'));

-- Festival de Otoño 2025: una unidad original + cuatro nuevas, todas habilitadas.
INSERT INTO unidad_venta (nombre_comercial, codigo, superficie, responsable_id, festival_id)
VALUES ('Food Truck Calandria', 'FTOTONIO02', 10.0, (SELECT id FROM personal WHERE email = 'demo-otono-01@festival.local'), @festival3_id);
SET @unidad12_id = LAST_INSERT_ID();
INSERT INTO food_truck (id, patente, esta_conectado) VALUES (@unidad12_id, 'DEF105', false);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad12_id, (SELECT id FROM personal WHERE email = 'demo-otono-01@festival.local'));

INSERT INTO unidad_venta (nombre_comercial, codigo, superficie, responsable_id, festival_id)
VALUES ('Puesto Otoño Norte', 'PDOTONIO02', 12.0, (SELECT id FROM personal WHERE email = 'demo-otono-02@festival.local'), @festival3_id);
SET @unidad13_id = LAST_INSERT_ID();
INSERT INTO puesto_desarmable (id, cantidad_carpas, tiempo_montaje) VALUES (@unidad13_id, 1, 20);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad13_id, (SELECT id FROM personal WHERE email = 'demo-otono-02@festival.local'));

INSERT INTO unidad_venta (nombre_comercial, codigo, superficie, responsable_id, festival_id)
VALUES ('Food Truck Otoño Sur', 'FTOTONIO03', 10.0, (SELECT id FROM personal WHERE email = 'demo-otono-03@festival.local'), @festival3_id);
SET @unidad14_id = LAST_INSERT_ID();
INSERT INTO food_truck (id, patente, esta_conectado) VALUES (@unidad14_id, 'DEF106', false);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad14_id, (SELECT id FROM personal WHERE email = 'demo-otono-03@festival.local'));

INSERT INTO unidad_venta (nombre_comercial, codigo, superficie, responsable_id, festival_id)
VALUES ('Puesto Otoño Oeste', 'PDOTONIO03', 12.0, (SELECT id FROM personal WHERE email = 'demo-otono-04@festival.local'), @festival3_id);
SET @unidad15_id = LAST_INSERT_ID();
INSERT INTO puesto_desarmable (id, cantidad_carpas, tiempo_montaje) VALUES (@unidad15_id, 1, 20);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad15_id, (SELECT id FROM personal WHERE email = 'demo-otono-04@festival.local'));

-- Cada unidad nueva vende 50 porciones a $20.000 con costo de $5.000: margen de $750.000 antes de canon y sueldos.
INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Menú Demo 2024 1', 5000, 20000, @unidad5_id);
SET @plato5_id = LAST_INSERT_ID();
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES ('2024-12-20', @festival_id, @unidad5_id);
SET @pedido5_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido5_id, @plato5_id, 50, 20000, 5000);

INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Menú Demo 2024 2', 5000, 20000, @unidad6_id);
SET @plato6_id = LAST_INSERT_ID();
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES ('2024-12-20', @festival_id, @unidad6_id);
SET @pedido6_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido6_id, @plato6_id, 50, 20000, 5000);

INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Menú Demo 2024 3', 5000, 20000, @unidad7_id);
SET @plato7_id = LAST_INSERT_ID();
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES ('2024-12-20', @festival_id, @unidad7_id);
SET @pedido7_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido7_id, @plato7_id, 50, 20000, 5000);

INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Menú Demo 2026 1', 5000, 20000, @unidad8_id);
SET @plato8_id = LAST_INSERT_ID();
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES (CURRENT_DATE, @festival2_id, @unidad8_id);
SET @pedido8_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido8_id, @plato8_id, 50, 20000, 5000);

INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Menú Demo 2026 2', 5000, 20000, @unidad9_id);
SET @plato9_id = LAST_INSERT_ID();
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES (CURRENT_DATE, @festival2_id, @unidad9_id);
SET @pedido9_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido9_id, @plato9_id, 50, 20000, 5000);

INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Menú Demo 2026 3', 5000, 20000, @unidad10_id);
SET @plato10_id = LAST_INSERT_ID();
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES (CURRENT_DATE, @festival2_id, @unidad10_id);
SET @pedido10_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido10_id, @plato10_id, 50, 20000, 5000);

INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Menú Demo 2026 4', 5000, 20000, @unidad11_id);
SET @plato11_id = LAST_INSERT_ID();
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES (CURRENT_DATE, @festival2_id, @unidad11_id);
SET @pedido11_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido11_id, @plato11_id, 50, 20000, 5000);

INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Menú Demo Otoño 1', 5000, 20000, @unidad12_id);
SET @plato12_id = LAST_INSERT_ID();
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES ('2025-04-10', @festival3_id, @unidad12_id);
SET @pedido12_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido12_id, @plato12_id, 50, 20000, 5000);

INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Menú Demo Otoño 2', 5000, 20000, @unidad13_id);
SET @plato13_id = LAST_INSERT_ID();
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES ('2025-04-10', @festival3_id, @unidad13_id);
SET @pedido13_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido13_id, @plato13_id, 50, 20000, 5000);

INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Menú Demo Otoño 3', 5000, 20000, @unidad14_id);
SET @plato14_id = LAST_INSERT_ID();
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES ('2025-04-10', @festival3_id, @unidad14_id);
SET @pedido14_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido14_id, @plato14_id, 50, 20000, 5000);

INSERT INTO plato (nombre, costo_produccion, precio_venta, unidad_venta_id) VALUES ('Menú Demo Otoño 4', 5000, 20000, @unidad15_id);
SET @plato15_id = LAST_INSERT_ID();
INSERT INTO pedido (fecha_pedido, festival_id, unidad_venta_id) VALUES ('2025-04-10', @festival3_id, @unidad15_id);
SET @pedido15_id = LAST_INSERT_ID();
INSERT INTO detalle_pedido (pedido_id, plato_id, cantidad, precio_unitario, costo_unitario) VALUES (@pedido15_id, @plato15_id, 50, 20000, 5000);

-- 28. Staff de las unidades de ejemplo: un cocinero y un cajero en cada una (pass: pass456).
--     Con las 50 porciones vendidas, todas las unidades quedan con rentabilidad neta positiva.
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Ramiro', 'Paz', 52000101, '1990-01-15', '2024-01-01', 'cocinero-pdandino01@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cocinero (id, especialidad, categoria) VALUES (@demo_empleado, 'Comida andina', 'COCINERO');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad4_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Pilar', 'Navarro', 52000102, '1990-01-15', '2024-01-01', 'cajero-pdandino01@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cajero (id, turno) VALUES (@demo_empleado, 'Mañana');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad4_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Sabrina', 'Ibáñez', 52000103, '1991-02-15', '2024-01-01', 'cocinero-ftdelsur02@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cocinero (id, especialidad, categoria) VALUES (@demo_empleado, 'Minutas', 'COCINERO');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad5_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Federico', 'Ruiz', 52000104, '1991-02-15', '2024-01-01', 'cajero-ftdelsur02@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cajero (id, turno) VALUES (@demo_empleado, 'Noche');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad5_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Gonzalo', 'Ríos', 52000105, '1992-03-15', '2024-01-01', 'cocinero-pdgrill002@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cocinero (id, especialidad, categoria) VALUES (@demo_empleado, 'Parrilla', 'COCINERO');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad6_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Milagros', 'Cabrera', 52000106, '1992-03-15', '2024-01-01', 'cajero-pdgrill002@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cajero (id, turno) VALUES (@demo_empleado, 'Mañana');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad6_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Florencia', 'Sosa', 52000107, '1993-04-15', '2024-01-01', 'cocinero-ftdelta001@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cocinero (id, especialidad, categoria) VALUES (@demo_empleado, 'Minutas', 'COCINERO');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad7_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Santiago', 'Godoy', 52000108, '1993-04-15', '2024-01-01', 'cajero-ftdelta001@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cajero (id, turno) VALUES (@demo_empleado, 'Noche');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad7_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Matías', 'Acosta', 52000109, '1994-05-15', '2024-01-01', 'cocinero-ftverano02@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cocinero (id, especialidad, categoria) VALUES (@demo_empleado, 'Hamburguesas', 'COCINERO');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad8_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Ailén', 'Giménez', 52000110, '1994-05-15', '2024-01-01', 'cajero-ftverano02@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cajero (id, turno) VALUES (@demo_empleado, 'Mañana');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad8_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Lucía', 'Benítez', 52000111, '1995-06-15', '2024-01-01', 'cocinero-pdprima002@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cocinero (id, especialidad, categoria) VALUES (@demo_empleado, 'Sándwiches', 'COCINERO');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad9_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Ignacio', 'Vera', 52000112, '1995-06-15', '2024-01-01', 'cajero-pdprima002@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cajero (id, turno) VALUES (@demo_empleado, 'Noche');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad9_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Ezequiel', 'Romero', 52000113, '1996-07-15', '2024-01-01', 'cocinero-ftprimav03@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cocinero (id, especialidad, categoria) VALUES (@demo_empleado, 'Minutas', 'COCINERO');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad10_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Rocío', 'Méndez', 52000114, '1996-07-15', '2024-01-01', 'cajero-ftprimav03@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cajero (id, turno) VALUES (@demo_empleado, 'Mañana');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad10_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Agustina', 'Medina', 52000115, '1997-08-15', '2024-01-01', 'cocinero-pdprimav03@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cocinero (id, especialidad, categoria) VALUES (@demo_empleado, 'Parrilla', 'COCINERO');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad11_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Hernán', 'Quiroga', 52000116, '1997-08-15', '2024-01-01', 'cajero-pdprimav03@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cajero (id, turno) VALUES (@demo_empleado, 'Noche');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad11_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Leandro', 'Ortiz', 52000117, '1998-09-15', '2024-01-01', 'cocinero-ftotonio02@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cocinero (id, especialidad, categoria) VALUES (@demo_empleado, 'Minutas', 'COCINERO');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad12_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Belén', 'Aguirre', 52000118, '1998-09-15', '2024-01-01', 'cajero-ftotonio02@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cajero (id, turno) VALUES (@demo_empleado, 'Mañana');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad12_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Carolina', 'Silva', 52000119, '1999-01-15', '2024-01-01', 'cocinero-pdotonio02@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cocinero (id, especialidad, categoria) VALUES (@demo_empleado, 'Guisos', 'COCINERO');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad13_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Marcos', 'Sánchez', 52000120, '1999-01-15', '2024-01-01', 'cajero-pdotonio02@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cajero (id, turno) VALUES (@demo_empleado, 'Noche');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad13_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Joaquín', 'Ponce', 52000121, '1990-02-15', '2024-01-01', 'cocinero-ftotonio03@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cocinero (id, especialidad, categoria) VALUES (@demo_empleado, 'Minutas', 'COCINERO');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad14_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Antonella', 'Ledesma', 52000122, '1990-02-15', '2024-01-01', 'cajero-ftotonio03@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cajero (id, turno) VALUES (@demo_empleado, 'Mañana');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad14_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Daniela', 'Ferreyra', 52000123, '1991-03-15', '2024-01-01', 'cocinero-pdotonio03@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cocinero (id, especialidad, categoria) VALUES (@demo_empleado, 'Guisos', 'COCINERO');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad15_id, @demo_empleado);
INSERT INTO personal (nombre, apellido, dni, fecha_nacimiento, fecha_ingreso, email, pass, esta_activo) VALUES ('Iván', 'Correa', 52000124, '1991-03-15', '2024-01-01', 'cajero-pdotonio03@festival.local', '$2a$10$YQsS0ezo6ru/rHt0oPQnfeKuGarsewMPybml7DLmRTMvv6dil0xgq', true);
SET @demo_empleado = LAST_INSERT_ID();
INSERT INTO empleado (id) VALUES (@demo_empleado);
INSERT INTO cajero (id, turno) VALUES (@demo_empleado, 'Noche');
INSERT INTO personal_rol (personal_id, rol_id) VALUES (@demo_empleado, @empleado_role);
INSERT INTO unidad_personal (unidad_id, personal_id) VALUES (@unidad15_id, @demo_empleado);
