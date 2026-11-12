-- =====================================================================
-- STORED PROCEDURES (MySQL Concurrency Control & Transactions)
-- College Course Registration System
-- =====================================================================

USE collegecourseregistrationsystem;

DROP PROCEDURE IF EXISTS RegisterStudentConcurrently;

DELIMITER //

CREATE PROCEDURE RegisterStudentConcurrently(
    IN p_student_id INT,
    IN p_section_id INT,
    OUT p_message VARCHAR(255)
)
proc: BEGIN
    DECLARE v_capacity INT DEFAULT 0;
    DECLARE v_enrolled INT DEFAULT 0;
    DECLARE v_duplicate INT DEFAULT 0;
    DECLARE v_new_id INT DEFAULT 800;

    -- 1. Check duplicate registration
    SELECT COUNT(*) INTO v_duplicate
    FROM Enrollment
    WHERE student_id = p_student_id AND section_id = p_section_id;

    IF v_duplicate > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'You are already registered for this course section!';
    END IF;

    -- 2. Start ACID Transaction directly inside MySQL
    START TRANSACTION;

    -- 3. CONCURRENCY: Acquire Exclusive Row-Level Lock (SELECT ... FOR UPDATE)
    -- If another student is registering at the exact same moment, MySQL holds them in queue!
    SELECT cl.capacity INTO v_capacity
    FROM Course_Section cs
    JOIN Classroom cl ON cs.classroom_id = cl.classroom_id
    WHERE cs.section_id = p_section_id
    FOR UPDATE;

    IF v_capacity IS NULL OR v_capacity = 0 THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Course section not found!';
    END IF;

    -- 4. Count currently enrolled students under exclusive lock protection
    SELECT COUNT(*) INTO v_enrolled
    FROM Enrollment
    WHERE section_id = p_section_id;

    -- 5. Seat Capacity Check: Prevent overbooking
    IF v_enrolled >= v_capacity THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Registration failed: Course is FULL! No seats available.';
    END IF;

    -- 6. Generate next enrollment ID
    SELECT COALESCE(MAX(enrollment_id), 800) + 1 INTO v_new_id
    FROM Enrollment;

    -- 7. Insert enrollment record
    INSERT INTO Enrollment (enrollment_id, student_id, section_id, enrollment_date, status, grade)
    VALUES (v_new_id, p_student_id, p_section_id, CURDATE(), 'Registered', NULL);

    -- 8. Commit transaction and release exclusive row lock
    COMMIT;

    SET p_message = CONCAT('Registered successfully in MySQL! Seat ', (v_enrolled + 1), '/', v_capacity, ' confirmed.');
END proc//

DELIMITER ;
