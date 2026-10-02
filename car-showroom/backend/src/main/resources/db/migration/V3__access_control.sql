-- Login code per showroom, typed on the login screen
ALTER TABLE showrooms ADD COLUMN code VARCHAR(30) NULL AFTER id;
UPDATE showrooms SET code = CONCAT('SR', id);
UPDATE showrooms SET code = 'DEMO' WHERE id = 1 AND name = 'Demo Showroom';
ALTER TABLE showrooms
  MODIFY code VARCHAR(30) NOT NULL,
  ADD UNIQUE KEY uq_showrooms_code (code);

ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP NULL AFTER password_hash;

-- Roles (per showroom)
CREATE TABLE roles (
  id          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  showroom_id BIGINT UNSIGNED NOT NULL,
  name        VARCHAR(60) NOT NULL,
  description VARCHAR(255),
  is_system   TINYINT(1) NOT NULL DEFAULT 0,
  created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at  TIMESTAMP NULL,
  UNIQUE KEY uq_roles_showroom_name (showroom_id, name),
  CONSTRAINT fk_roles_showroom FOREIGN KEY (showroom_id) REFERENCES showrooms(id)
) ENGINE=InnoDB;

-- Permissions (global, seeded by the system; one per screen/action)
CREATE TABLE permissions (
  id          SMALLINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  code        VARCHAR(60) NOT NULL UNIQUE,
  label       VARCHAR(100) NOT NULL,
  module      VARCHAR(40) NOT NULL,
  url_pattern VARCHAR(150) NULL
) ENGINE=InnoDB;

CREATE TABLE role_permissions (
  role_id       BIGINT UNSIGNED NOT NULL,
  permission_id SMALLINT UNSIGNED NOT NULL,
  PRIMARY KEY (role_id, permission_id),
  KEY idx_rp_permission (permission_id),
  CONSTRAINT fk_rp_role       FOREIGN KEY (role_id)       REFERENCES roles(id) ON DELETE CASCADE,
  CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions(id)
) ENGINE=InnoDB;

CREATE TABLE user_roles (
  user_id     BIGINT UNSIGNED NOT NULL,
  role_id     BIGINT UNSIGNED NOT NULL,
  assigned_by BIGINT UNSIGNED NULL,
  assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, role_id),
  KEY idx_ur_role (role_id),
  CONSTRAINT fk_ur_user        FOREIGN KEY (user_id)     REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_ur_role        FOREIGN KEY (role_id)     REFERENCES roles(id) ON DELETE CASCADE,
  CONSTRAINT fk_ur_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(id)
) ENGINE=InnoDB;

INSERT INTO permissions (code, label, module, url_pattern) VALUES
  ('VEHICLE_VIEW',     'View vehicles',                'Vehicles', '/vehicles'),
  ('VEHICLE_CREATE',   'Add vehicles',                 'Vehicles', '/vehicles/new'),
  ('VEHICLE_EDIT',     'Edit vehicles',                'Vehicles', '/vehicles/:id/edit'),
  ('VEHICLE_DELETE',   'Delete vehicles',              'Vehicles', NULL),
  ('USER_VIEW',        'View users',                   'Users',    '/users'),
  ('USER_CREATE',      'Add users',                    'Users',    '/users/new'),
  ('USER_EDIT',        'Edit users',                   'Users',    '/users/:id/edit'),
  ('USER_DEACTIVATE',  'Activate / deactivate users',  'Users',    NULL),
  ('USER_ASSIGN_ROLE', 'Assign roles to users',        'Users',    NULL),
  ('ROLE_VIEW',        'View roles',                   'Roles',    '/roles'),
  ('ROLE_MANAGE',      'Create / edit / delete roles', 'Roles',    '/roles/new');

-- Every showroom gets a built-in Admin role with every permission.
INSERT INTO roles (showroom_id, name, description, is_system)
SELECT id, 'Admin', 'Full access to everything', 1 FROM showrooms;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.is_system = 1;
