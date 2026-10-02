-- Demo data so the screens are not empty on first run. Delete this file before
-- the first real deployment (or keep it out of production profiles).
INSERT INTO showrooms (id, name, phone, address)
VALUES (1, 'Demo Showroom', '0300-0000000', 'Karachi');

INSERT INTO users (id, showroom_id, user_type_id, name, email, phone) VALUES
  (1, 1, (SELECT id FROM user_types WHERE code = 'partner'),  'Ali Khan',    'ali@example.com',   '0300-1111111'),
  (2, 1, (SELECT id FROM user_types WHERE code = 'investor'), 'Sara Ahmed',  'sara@example.com',  '0301-2222222'),
  (3, 1, (SELECT id FROM user_types WHERE code = 'investor'), 'Bilal Raza',  'bilal@example.com', '0302-3333333');

INSERT INTO vehicles (id, showroom_id, wheels, model_no, model_year, number_plate, purchase_price, purchase_date, notes) VALUES
  (1, 1, 4, 'Corolla GLi', 2019, 'ABC-123',  3500000.00, '2025-11-10', NULL),
  (2, 1, 2, 'CG 125',      2022, 'KLM-4567',  250000.00, '2026-02-03', NULL),
  (3, 1, 6, 'Hino 500',    2018, 'TRK-9001', 9800000.00, '2026-05-20', 'Dump truck');

INSERT INTO vehicle_investors (vehicle_id, user_id, amount_invested, invested_on) VALUES
  (1, 2, 2000000.00, '2025-11-10'),
  (1, 3, 1500000.00, '2025-11-10'),
  (2, 2,  250000.00, '2026-02-03'),
  (3, 1, 3000000.00, '2026-05-20'),
  (3, 3, 6800000.00, '2026-05-20');
