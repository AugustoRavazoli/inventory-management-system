DROP VIEW IF EXISTS dashboard;
DROP TABLE IF EXISTS order_sequence;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS "order";
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS password_reset_token;
DROP TABLE IF EXISTS verification_token;
DROP TABLE IF EXISTS "user";

CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password CHAR(60) NOT NULL,
    status VARCHAR(255) NOT NULL CHECK (status IN ('ACTIVE', 'DELETED', 'UNVERIFIED'))
);

CREATE TABLE verification_token (
    user_id BIGINT PRIMARY KEY REFERENCES "user"(id) ON DELETE CASCADE,
    token CHAR(36) NOT NULL UNIQUE,
    expiration_time TIMESTAMP NOT NULL
);

CREATE TABLE password_reset_token (
    user_id BIGINT PRIMARY KEY REFERENCES "user"(id),
    token CHAR(36) NOT NULL UNIQUE,
    expiration_time TIMESTAMP NOT NULL
);

CREATE OR REPLACE PROCEDURE delete_expired_password_reset_tokens()
    LANGUAGE PLPGSQL
AS
'
BEGIN
    DELETE FROM password_reset_token prt
    WHERE prt.expiration_time + INTERVAL ''24 hours'' < CURRENT_TIMESTAMP;
END;
';

CREATE OR REPLACE PROCEDURE delete_unverified_users_with_expired_tokens()
    LANGUAGE PLPGSQL
AS
'
BEGIN
    DELETE FROM "user" u
    USING verification_token vt
    WHERE u.id = vt.user_id
        AND u.status = ''UNVERIFIED''
        AND vt.expiration_time + INTERVAL ''24 hours'' < CURRENT_TIMESTAMP;
END;
';

CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner_id BIGINT NOT NULL REFERENCES "user"(id),
    UNIQUE(name, owner_id)
);

CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category_id BIGINT REFERENCES category(id) ON DELETE SET NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    price DECIMAL(12, 2) NOT NULL CHECK (price >= 0.01),
    owner_id BIGINT NOT NULL REFERENCES "user"(id),
    UNIQUE(name, owner_id)
);

CREATE TABLE customer (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    owner_id BIGINT NOT NULL REFERENCES "user"(id),
    UNIQUE(name, owner_id)
);

CREATE TABLE "order" (
    id BIGSERIAL PRIMARY KEY,
    number INTEGER NOT NULL,
    status VARCHAR(255) NOT NULL CHECK (status IN ('UNPAID', 'PAID')),
    "date" date NOT NULL DEFAULT CURRENT_DATE,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    owner_id BIGINT NOT NULL REFERENCES "user"(id),
    UNIQUE(number, owner_id)
);

CREATE TABLE order_item (
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
    order_id BIGINT NOT NULL REFERENCES "order"(id) ON DELETE CASCADE,
    "index" INTEGER NOT NULL,
    PRIMARY KEY (product_id, order_id)
);

CREATE OR REPLACE FUNCTION calculate_total_sales(p_owner_id BIGINT)
    RETURNS DECIMAL(12, 2)
    IMMUTABLE
    LANGUAGE PLPGSQL
AS
'
DECLARE
    v_total DECIMAL(12, 2);
BEGIN
   SELECT SUM(p.price * oi.quantity)
   INTO v_total
   FROM order_item oi
   INNER JOIN product p
   ON oi.product_id = p.id
   INNER JOIN "order" o
   ON oi.order_id = o.id
   WHERE o.status = ''PAID'' AND o.owner_id = p_owner_id;

   RETURN COALESCE(v_total, 0.00);
END;
';

CREATE VIEW dashboard AS
    SELECT
        (SELECT COUNT(id) FROM customer WHERE owner_id = u.id) AS total_customers,
        (SELECT COUNT(id) FROM category WHERE owner_id = u.id) AS total_categories,
        (SELECT COUNT(id) FROM product WHERE owner_id = u.id) AS total_products,
        (SELECT COUNT(id) FROM "order" WHERE status = 'UNPAID' AND owner_id = u.id) AS total_unpaid_orders,
        (SELECT COUNT(id) FROM "order" WHERE status = 'PAID' AND owner_id = u.id) AS total_paid_orders,
        (SELECT calculate_total_sales(u.id)) AS total_sales,
        u.id AS owner_id
    FROM
        "user" u;

CREATE TABLE order_sequence (
    owner_id BIGINT PRIMARY KEY REFERENCES "user"(id) ON DELETE CASCADE,
    counter INTEGER NOT NULL
);

CREATE OR REPLACE FUNCTION next_order_number(p_owner_id BIGINT)
    RETURNS INTEGER
    LANGUAGE PLPGSQL
    VOLATILE
AS
'
DECLARE
    v_counter INTEGER;
BEGIN
    INSERT INTO order_sequence(owner_id, counter)
    VALUES (p_owner_id, 1)
    ON CONFLICT(owner_id)
    DO UPDATE SET counter = order_sequence.counter + 1
    RETURNING counter INTO v_counter;
    RETURN v_counter;
END;
';

CREATE OR REPLACE FUNCTION generate_order_number()
    RETURNS TRIGGER
    LANGUAGE PLPGSQL
AS
'
BEGIN
    NEW.number := next_order_number(NEW.owner_id);
    RETURN NEW;
END;
';

CREATE OR REPLACE TRIGGER base_table_insert_trigger
    BEFORE INSERT ON "order"
    FOR EACH ROW
    EXECUTE PROCEDURE generate_order_number();
