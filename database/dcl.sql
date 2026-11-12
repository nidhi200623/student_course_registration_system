-- =====================================================================
-- DCL COMMANDS (Data Control Language)
-- College Course Registration System
-- =====================================================================
-- S.No | Command | Purpose                 | Syntax                              | Example                           | Output
-- -----------------------------------------------------------------------------------------------------------------------------
-- 1.   | GRANT   | Gives permission to user| GRANT privilege ON object TO user;  | GRANT SELECT ON Student TO user1; | Privilege granted.
-- 2.   | GRANT   | Gives all permissions   | GRANT ALL ON object TO user;        | GRANT ALL ON Student TO user1;    | Privileges granted.
-- 3.   | REVOKE  | Removes permission      | REVOKE privilege ON object FROM user| REVOKE SELECT ON Student FROM user1; Privilege revoked.
-- =====================================================================

USE collegecourseregistrationsystem;

-- Create test user for DCL demonstration
CREATE USER IF NOT EXISTS 'user1'@'localhost' IDENTIFIED BY 'password123';

-- ---------------------------------------------------------------------
-- 1. GRANT (Gives specific permission to a user)
-- Syntax:  GRANT privilege ON object TO user;
-- Example: GRANT SELECT ON Student TO user1;
-- ---------------------------------------------------------------------
GRANT SELECT ON Student TO 'user1'@'localhost';
FLUSH PRIVILEGES;

-- Verify granted privileges
SHOW GRANTS FOR 'user1'@'localhost';

-- ---------------------------------------------------------------------
-- 2. GRANT ALL (Gives all permissions on object to a user)
-- Syntax:  GRANT ALL ON object TO user;
-- Example: GRANT ALL ON Student TO user1;
-- ---------------------------------------------------------------------
GRANT ALL ON Student TO 'user1'@'localhost';
FLUSH PRIVILEGES;

-- Verify all privileges granted
SHOW GRANTS FOR 'user1'@'localhost';

-- ---------------------------------------------------------------------
-- 3. REVOKE (Removes permission from a user)
-- Syntax:  REVOKE privilege ON object FROM user;
-- Example: REVOKE SELECT ON Student FROM user1;
-- ---------------------------------------------------------------------
REVOKE SELECT ON Student FROM 'user1'@'localhost';
FLUSH PRIVILEGES;

-- Verify privilege revoked
SHOW GRANTS FOR 'user1'@'localhost';

-- =====================================================================
-- ROLE-BASED DCL FOR THREE LOGINS IN THE PROJECT:
-- 1. Student User: Can view courses, sections and manage own enrollment
-- 2. Faculty User: Can view teaching sections and update grades
-- 3. Admin User:   Full access to all tables
-- =====================================================================

-- 1. Student User
CREATE USER IF NOT EXISTS 'student_user'@'localhost' IDENTIFIED BY 'student123';
GRANT SELECT ON Course TO 'student_user'@'localhost';
GRANT SELECT ON Course_Offering TO 'student_user'@'localhost';
GRANT SELECT ON Course_Section TO 'student_user'@'localhost';
GRANT SELECT ON Classroom TO 'student_user'@'localhost';
GRANT SELECT ON Faculty TO 'student_user'@'localhost';
GRANT SELECT, INSERT, DELETE ON Enrollment TO 'student_user'@'localhost';

-- 2. Faculty User
CREATE USER IF NOT EXISTS 'faculty_user'@'localhost' IDENTIFIED BY 'faculty123';
GRANT SELECT ON Course_Section TO 'faculty_user'@'localhost';
GRANT SELECT ON Course_Offering TO 'faculty_user'@'localhost';
GRANT SELECT ON Course TO 'faculty_user'@'localhost';
GRANT SELECT ON Student TO 'faculty_user'@'localhost';
GRANT SELECT, UPDATE(grade, status) ON Enrollment TO 'faculty_user'@'localhost';

-- 3. Administrator User (All permissions)
CREATE USER IF NOT EXISTS 'admin_user'@'localhost' IDENTIFIED BY 'admin123';
GRANT ALL PRIVILEGES ON collegecourseregistrationsystem.* TO 'admin_user'@'localhost';

FLUSH PRIVILEGES;

-- Display grants for all project users
SHOW GRANTS FOR 'student_user'@'localhost';
SHOW GRANTS FOR 'faculty_user'@'localhost';
SHOW GRANTS FOR 'admin_user'@'localhost';
