
USE collegecourseregistrationsystem;

-- EXCLUSIVE LOCK: CONNECTION A

START TRANSACTION;

-- Acquire exclusive lock on enrollment 801
SELECT *
FROM Enrollment
WHERE enrollment_id = 801
FOR UPDATE;

-- Keep transaction open while Connection B
-- attempts to acquire the same exclusive lock.

-- Release the exclusive lock after Connection B waits
COMMIT;
