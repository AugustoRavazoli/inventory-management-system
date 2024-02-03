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
    password CHAR(60) NOT NULL
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
    status VARCHAR(255) NOT NULL CHECK (status IN ('UNPAID', 'PAID')),
    "date" date NOT NULL DEFAULT CURRENT_DATE,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    owner_id BIGINT NOT NULL REFERENCES "user"(id)
);

CREATE TABLE order_item (
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
    order_id BIGINT NOT NULL REFERENCES "order"(id) ON DELETE CASCADE,
    "index" INTEGER NOT NULL,
    PRIMARY KEY (product_id, order_id)
);
