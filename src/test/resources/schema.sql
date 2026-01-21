CREATE TABLE IF NOT EXISTS "user" (
    user_name VARCHAR(255) NOT NULL PRIMARY KEY,
    password VARCHAR(255),
    saved_time DATETIME
);

CREATE TABLE IF NOT EXISTS amount_type (
    amount_type_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL,
    type_name VARCHAR(255),
    saved_time DATETIME,
    deleted BOOLEAN,
    CONSTRAINT fk_amount_type_user FOREIGN KEY (user_name)
        REFERENCES "user"(user_name)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS category (
    category_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL,
    category_name VARCHAR(255),
    saved_time DATETIME,
    deleted BOOLEAN,
    CONSTRAINT fk_category_user FOREIGN KEY (user_name)
        REFERENCES "user"(user_name)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS shopping_item (
    shopping_item_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(255) NOT NULL,
    amount_type_id BIGINT,
    category_id BIGINT,
    item_name VARCHAR(255),
    amount DOUBLE,
    bought BOOLEAN,
    saved_time DATETIME,
    deleted BOOLEAN,

    CONSTRAINT fk_si_user FOREIGN KEY (user_name)
        REFERENCES "user"(user_name)
        ON DELETE CASCADE,

    CONSTRAINT fk_si_amt FOREIGN KEY (amount_type_id)
        REFERENCES amount_type(amount_type_id)
        ON DELETE SET NULL,

    CONSTRAINT fk_si_cat FOREIGN KEY (category_id)
        REFERENCES category(category_id)
        ON DELETE SET NULL
);

CREATE SEQUENCE IF NOT EXISTS user_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS amount_type_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS category_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS shopping_item_seq START WITH 1 INCREMENT BY 50;
