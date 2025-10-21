## 1. ER-модель
![ER диаграмма](https://raw.githubusercontent.com/arseeenyyy/JArch/main/docs/db_structure/ER.svg)

## 2. Даталогическая модель
![Даталогическая модель](https://raw.githubusercontent.com/arseeenyyy/JArch/main/docs/db_structure/data.svg)

## 3. Реализация даталогической модели в субд Postgresql
``` sql
-- Таблица пользователей
CREATE TABLE "User" (
    username VARCHAR(15) PRIMARY KEY,
    email TEXT NOT NULL
);

-- Таблица ролей
CREATE TABLE Role (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT
);

-- Системные роли пользователей
CREATE TABLE SystemUserRole (
    id SERIAL PRIMARY KEY,
    username VARCHAR(15) NOT NULL REFERENCES "User"(username) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES Role(id) ON DELETE CASCADE
);

-- Таблица проектов
CREATE TABLE Project (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    description TEXT,
    minio_id INT UNIQUE NOT NULL,
    date_created DATE NOT NULL,
    date_updated DATE
);

-- Назначение ролей пользователям в проектах
CREATE TABLE ProjectUserRole (
    id SERIAL PRIMARY KEY,
    username VARCHAR(15) NOT NULL REFERENCES "User"(username) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES Role(id) ON DELETE CASCADE,
    project_id INT NOT NULL REFERENCES Project(id) ON DELETE CASCADE
);

-- Таблица бэкапов
CREATE TABLE Backup (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    description TEXT,
    minio_id INT UNIQUE NOT NULL,
    project_id INT NOT NULL REFERENCES Project(id) ON DELETE CASCADE,
    date_created DATE NOT NULL
);

-- Таблица пресетов
CREATE TABLE Presets (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    description TEXT,
    username VARCHAR(15) NOT NULL REFERENCES "User"(username) ON DELETE CASCADE,
    minio_id INT UNIQUE NOT NULL
);
```
## 4. Целостность данных 
``` sql
CREATE OR REPLACE FUNCTION update_project_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.date_updated := CURRENT_DATE;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_project_update
BEFORE UPDATE ON Project
FOR EACH ROW
EXECUTE FUNCTION update_project_timestamp();
```

## 5. Скрипты для создания, удаления базы данных, заполнения базы тестовыми данными.
``` sql
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
```

## 6. Функции и процедуры для выполнения критически важных запросов
```sql
CREATE OR REPLACE FUNCTION get_user_projects(p_username VARCHAR)
RETURNS TABLE (project_id INT, project_name TEXT) AS $$
BEGIN
    RETURN QUERY
    SELECT p.id, p.name
    FROM Project p
    JOIN ProjectUserRole pur ON pur.project_id = p.id
    WHERE pur.username = p_username;
END;
$$ LANGUAGE plpgsql;
```

## 7. Индексы
```sql
-- Быстрый доступ к ролям пользователей
CREATE INDEX idx_system_user_role_username ON SystemUserRole(username);

-- Быстрый поиск пользователей в проекте
CREATE INDEX idx_project_user_role_project_id ON ProjectUserRole(project_id);

-- Быстрый поиск по бэкапам проекта
CREATE INDEX idx_backup_project_id ON Backup(project_id);

-- Быстрый доступ к пресетам пользователя
CREATE INDEX idx_presets_username ON Presets(username);
```
