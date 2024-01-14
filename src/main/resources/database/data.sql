INSERT INTO "user" (name, email, password) VALUES
    ('user', 'user@email.com', '$2a$10$ngSdw1kgIS40jwcvUqw48Osyd9NE8PjuMUatovpt6hlCBF0TDFUJu');

INSERT INTO category (name, owner_id) VALUES
    ('A', 1),
    ('B', 1),
    ('C', 1),
    ('D', 1),
    ('E', 1),
    ('F', 1),
    ('G', 1),
    ('H', 1);

INSERT INTO product (name, category_id, quantity, price, owner_id) VALUES
    ('A', 1, 50, '100', 1),
    ('B', 2, 100, '150', 1),
    ('C', 3, 150, '200', 1),
    ('D', 4, 200, '250', 1),
    ('E', 5, 250, '300', 1),
    ('F', 6, 300, '350', 1),
    ('G', 7, 350, '400', 1),
    ('H', 8, 400, '450', 1),
    ('I', 1, 450, '500', 1),
    ('J', 2, 500, '550', 1),
    ('K', 3, 550, '600', 1),
    ('L', 4, 600, '650', 1),
    ('M', 5, 650, '700', 1),
    ('N', 6, 700, '750', 1),
    ('O', 7, 750, '800', 1),
    ('P', 8, 800, '850', 1),
    ('Q', 1, 850, '900', 1),
    ('R', 2, 900, '950', 1),
    ('S', 3, 950, '1000', 1),
    ('T', 4, 1000, '1050', 1);

INSERT INTO customer (name, address, phone) VALUES
    ('A', 'A', 'A'),
    ('B', 'B', 'B'),
    ('C', 'C', 'C'),
    ('D', 'D', 'D'),
    ('E', 'E', 'E'),
    ('F', 'F', 'F'),
    ('G', 'G', 'G'),
    ('H', 'H', 'H');

INSERT INTO "order" (status, customer_id) VALUES
    ('UNPAID', 1),
    ('UNPAID', 2),
    ('UNPAID', 3),
    ('UNPAID', 4),
    ('PAID', 5),
    ('PAID', 6);

INSERT INTO order_item (quantity, product_id, order_id) VALUES
    (1, 1, 1),
    (2, 2, 1),
    (3, 3, 1),
    (1, 1, 2),
    (2, 2, 2),
    (3, 3, 2),
    (1, 1, 3),
    (2, 2, 3),
    (3, 3, 3),
    (1, 1, 4),
    (2, 2, 4),
    (3, 3, 4),
    (1, 1, 5),
    (2, 2, 5),
    (3, 3, 5),
    (1, 1, 6),
    (2, 2, 6),
    (3, 3, 6);
