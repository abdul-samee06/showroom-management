-- Customers (showrooms)
CREATE TABLE showrooms (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(150) NOT NULL,
  phone       VARCHAR(30),
  email       VARCHAR(150),
  address     VARCHAR(255),
  is_active   TINYINT(1) NOT NULL DEFAULT 1,
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at  TIMESTAMP NULL
) ENGINE=InnoDB;

-- Lookup: partner, investor, ... (extendable)
CREATE TABLE user_types (
  id     SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  code   VARCHAR(30) NOT NULL UNIQUE,
  label  VARCHAR(60) NOT NULL
) ENGINE=InnoDB;

INSERT INTO user_types (code, label) VALUES
  ('partner',  'Partner'),
  ('investor', 'Investor');

-- Users belonging to a showroom
CREATE TABLE users (
  id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  showroom_id   BIGINT UNSIGNED NOT NULL,
  user_type_id  SMALLINT UNSIGNED NOT NULL,
  name          VARCHAR(150) NOT NULL,
  email         VARCHAR(150),
  phone         VARCHAR(30),
  password_hash VARCHAR(255),
  is_active     TINYINT(1) NOT NULL DEFAULT 1,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at    TIMESTAMP NULL,
  UNIQUE KEY uq_users_showroom_email (showroom_id, email),
  KEY idx_users_type (user_type_id),
  CONSTRAINT fk_users_showroom FOREIGN KEY (showroom_id) REFERENCES showrooms(id),
  CONSTRAINT fk_users_type     FOREIGN KEY (user_type_id) REFERENCES user_types(id)
) ENGINE=InnoDB;

-- Vehicles
CREATE TABLE vehicles (
  id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  showroom_id     BIGINT UNSIGNED NOT NULL,
  wheels          TINYINT UNSIGNED NOT NULL,
  model_no        VARCHAR(60) NOT NULL,
  model_year      SMALLINT UNSIGNED NOT NULL,
  number_plate    VARCHAR(30) NOT NULL,
  purchase_price  DECIMAL(14,2) NOT NULL,
  purchase_date   DATE NOT NULL,
  notes           TEXT,
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at      TIMESTAMP NULL,
  UNIQUE KEY uq_vehicle_plate (showroom_id, number_plate),
  KEY idx_vehicle_model (model_no, model_year),
  CONSTRAINT fk_vehicles_showroom FOREIGN KEY (showroom_id) REFERENCES showrooms(id),
  CONSTRAINT chk_vehicle_wheels CHECK (wheels IN (2, 4, 6)),
  CONSTRAINT chk_vehicle_price  CHECK (purchase_price >= 0)
) ENGINE=InnoDB;

-- Who invested in which vehicle (many-to-many)
CREATE TABLE vehicle_investors (
  id               BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  vehicle_id       BIGINT UNSIGNED NOT NULL,
  user_id          BIGINT UNSIGNED NOT NULL,
  amount_invested  DECIMAL(14,2) NOT NULL,
  invested_on      DATE NULL,
  created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_vehicle_user (vehicle_id, user_id),
  KEY idx_vi_user (user_id),
  CONSTRAINT fk_vi_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
  CONSTRAINT fk_vi_user    FOREIGN KEY (user_id)    REFERENCES users(id),
  CONSTRAINT chk_vi_amount CHECK (amount_invested > 0)
) ENGINE=InnoDB;
