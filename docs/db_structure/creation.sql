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
