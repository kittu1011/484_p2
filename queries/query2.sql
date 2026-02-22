SELECT u.user_id, u.first_name, u.last_name
FROM project2.Public_Users u
WHERE u.user_id NOT IN (
    SELECT f1.user1_id FROM project2.Public_Friends f1
    UNION
    SELECT f2.user2_id FROM project2.Public_Friends f2
)
ORDER BY u.user_id ASC;