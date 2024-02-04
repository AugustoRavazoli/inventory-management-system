DROP TABLE IF EXISTS order_sequence;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS "order";
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS "user";

CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password CHAR(60) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

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
