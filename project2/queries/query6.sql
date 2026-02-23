--create view
CREATE VIEW bidirectional AS
SELECT user1_id, user2_id FROM project2.Public_Friends
UNION
SELECT user2_id, user1_id FROM project2.Public_Friends;
--query a
SELECT u1.user_id, u1.first_name, u1.last_name, u2.user_id, u2.first_name, u2.last_name
FROM project2.Public_Users u1
JOIN project2.Public_Users u2 ON u1.user_id < u2.user_id
JOIN bidirectional b1 ON u1.user_id = b1.user1_id
JOIN bidirectional b2 ON u2.user_id = b2.user1_id AND b1.user2_id = b2.user2_id
WHERE NOT EXISTS (
    SELECT 1 
    FROM bidirectional b 
    WHERE u1.user_id = b.user1_id AND u2.user_id = b.user2_id
)
GROUP BY u1.user_id, u1.first_name, u1.last_name, u2.user_id, u2.first_name, u2.last_name
ORDER BY COUNT(*) DESC, u1.user_id ASC, u2.user_id ASC;
--query b Replace U1 and U2 with java variables
SELECT DISTINCT u.user_id, u.first_name, u.last_name
FROM bidirectional b1
JOIN bidirectional b2 ON b1.user2_id = b2.user1_id
JOIN project2.Public_Users u ON b1.user2_id = u.user_id
WHERE b1.user1_id = U1 AND b2.user2_id = U2
ORDER BY u.user_id ASC;

DROP VIEW bidirectional;
