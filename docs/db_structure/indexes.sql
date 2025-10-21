-- Быстрый доступ к ролям пользователей
CREATE INDEX idx_system_user_role_username ON SystemUserRole(username);

-- Быстрый поиск пользователей в проекте
CREATE INDEX idx_project_user_role_project_id ON ProjectUserRole(project_id);

-- Быстрый поиск по бэкапам проекта
CREATE INDEX idx_backup_project_id ON Backup(project_id);

-- Быстрый доступ к пресетам пользователя
CREATE INDEX idx_presets_username ON Presets(username);
