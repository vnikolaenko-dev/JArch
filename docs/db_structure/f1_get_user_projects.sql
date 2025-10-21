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
