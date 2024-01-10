INSERT INTO category (name) VALUES
    ('A'),
    ('B'),
    ('C'),
    ('D'),
    ('E'),
    ('F'),
    ('G'),
    ('H') ON CONFLICT (name) DO NOTHING;

INSERT INTO product (name, category_id, quantity, price) VALUES
    ('A', 1, 50, '100'),
    ('B', 2, 100, '150'),
    ('C', 3, 150, '200'),
    ('D', 4, 200, '250'),
    ('E', 5, 250, '300'),
    ('F', 6, 300, '350'),
    ('G', 7, 350, '400'),
    ('H', 8, 400, '450'),
    ('I', 1, 450, '500'),
    ('J', 2, 500, '550'),
    ('K', 3, 550, '600'),
    ('L', 4, 600, '650'),
    ('M', 5, 650, '700'),
    ('N', 6, 700, '750'),
    ('O', 7, 750, '800'),
    ('P', 8, 800, '850'),
    ('Q', 1, 850, '900'),
    ('R', 2, 900, '950'),
    ('S', 3, 950, '1000'),
    ('T', 4, 1000, '1050') ON CONFLICT (name) DO NOTHING;

INSERT INTO customer (name, address, phone) VALUES
    ('A', 'A', 'A'),
    ('B', 'B', 'B'),
    ('C', 'C', 'C'),
    ('D', 'D', 'D'),
    ('E', 'E', 'E'),
    ('F', 'F', 'F'),
    ('G', 'G', 'G'),
    ('H', 'H', 'H') ON CONFLICT (name) DO NOTHING;

INSERT INTO "order" (id, status, customer_id) VALUES
    (1, 'UNPAID', 1),
    (2, 'UNPAID', 2),
    (3, 'UNPAID', 3),
    (4, 'UNPAID', 4),
    (5, 'PAID', 5),
    (6, 'PAID', 6) ON CONFLICT (id) DO NOTHING;

INSERT INTO order_item (id, quantity, product_id, order_id) VALUES
    (1, 1, 1, 1),
    (2, 2, 2, 1),
    (3, 3, 3, 1),
    (4, 1, 1, 2),
    (5, 2, 2, 2),
    (6, 3, 3, 2),
    (7, 1, 1, 3),
    (8, 2, 2, 3),
    (9, 3, 3, 3),
    (10, 1, 1, 4),
    (11, 2, 2, 4),
    (12, 3, 3, 4),
    (13, 1, 1, 5),
    (14, 2, 2, 5),
    (15, 3, 3, 5),
    (16, 1, 1, 6),
    (17, 2, 2, 6),
    (18, 3, 3, 6) ON CONFLICT (id) DO NOTHING;
