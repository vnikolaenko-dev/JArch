-- Пользователи
INSERT INTO "User" (username, email) VALUES
('alice', 'alice@example.com'),
('bob', 'bob@example.com');

-- Роли
INSERT INTO Role (name, description) VALUES
('admin', 'System administrator'),
('manager', 'Project manager'),
('developer', 'Developer');

-- Системные роли
INSERT INTO SystemUserRole (username, role_id) VALUES
('alice', 1),
('bob', 2);

-- Проекты
INSERT INTO Project (name, description, minio_id, date_created, date_updated) VALUES
('Project Alpha', 'First project', 1001, CURRENT_DATE, CURRENT_DATE),
('Project Beta', 'Second project', 1002, CURRENT_DATE, CURRENT_DATE);

-- Роли в проектах
INSERT INTO ProjectUserRole (username, role_id, project_id) VALUES
('alice', 1, 1),
('bob', 3, 2);

-- Бэкапы
INSERT INTO Backup (name, description, minio_id, project_id, date_created) VALUES
('Backup 1', 'Daily backup', 2001, 1, CURRENT_DATE),
('Backup 2', 'Weekly backup', 2002, 2, CURRENT_DATE);

-- Пресеты
INSERT INTO Presets (name, description, username, minio_id) VALUES
('Preset A', 'Some config', 'alice', 3001),
('Preset B', 'Another config', 'bob', 3002);
