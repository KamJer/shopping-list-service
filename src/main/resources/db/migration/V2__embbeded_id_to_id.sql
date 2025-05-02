START TRANSACTION;

-- 1. Rename old tables (for backup/reference)
RENAME TABLE CATEGORY TO CATEGORY_OLD;
RENAME TABLE AMOUNT_TYPE TO AMOUNT_TYPE_OLD;
RENAME TABLE SHOPPING_ITEM TO SHOPPING_ITEM_OLD;

CREATE TABLE AMOUNT_TYPE (
    amount_type_id BIGINT NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(255) NOT NULL,
    type_name VARCHAR(255),
    saved_time DATETIME,
    deleted BIT(1) NOT NULL DEFAULT b'0',

    PRIMARY KEY (amount_type_id),
    CONSTRAINT fk_amount_type_user FOREIGN KEY (user_name) REFERENCES `USER`(user_name) ON DELETE CASCADE
);

INSERT INTO AMOUNT_TYPE (amount_type_id, user_name, type_name, saved_time, deleted)
SELECT amount_type_id, user_name, type_name, saved_time, deleted FROM AMOUNT_TYPE_OLD;

-- 2. Create new `category` table with flat ID
CREATE TABLE CATEGORY (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_name VARCHAR(255) NOT NULL,
    category_name VARCHAR(255),
    saved_time DATETIME,
    deleted BIT NOT NULL DEFAULT b'0',
    CONSTRAINT fk_category_user FOREIGN KEY (user_name) REFERENCES `USER`(user_name) ON DELETE CASCADE
);

-- 3. Copy data from old to new
INSERT INTO CATEGORY (category_id, user_name, category_name, saved_time, deleted)
SELECT category_id, user_name, category_name, saved_time, deleted FROM CATEGORY_OLD;

-- 4. Create new `shopping_item` table with flat ID
CREATE TABLE SHOPPING_ITEM (
    shopping_item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_name VARCHAR(255) NOT NULL,
    amount_type_id BIGINT,
    category_id BIGINT,
    item_name VARCHAR(255),
    amount DOUBLE,
    bought BIT NOT NULL DEFAULT b'0',
    saved_time DATETIME,
    deleted BIT NOT NULL DEFAULT b'0',
    CONSTRAINT fk_si_user FOREIGN KEY (user_name) REFERENCES `USER`(user_name),
    CONSTRAINT fk_si_cat FOREIGN KEY (category_id) REFERENCES CATEGORY(category_id),
    CONSTRAINT fk_si_amt FOREIGN KEY (amount_type_id) REFERENCES AMOUNT_TYPE(amount_type_id) -- update if needed
);

-- 5. Copy data
INSERT INTO SHOPPING_ITEM (shopping_item_id, user_name, amount_type_id, category_id, item_name, amount, bought, saved_time, deleted)
SELECT shopping_item_id, user_name, amount_type_id, category_id, item_name, amount, bought, saved_time, deleted
FROM shopping_item_old;

COMMIT;
