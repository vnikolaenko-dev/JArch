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
