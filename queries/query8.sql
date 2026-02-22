--REPLACE userID with java variable
--oldest query
SELECT u.user_id, u.first_name, u.last_name
FROM project2.Public_Users u
JOIN (
    SELECT user1_id AS friend_id FROM project2.Public_Friends WHERE user2_id = userID
    UNION
    SELECT user2_id AS friend_id FROM project2.Public_Friends WHERE user1_id = userID
) f ON u.user_id = f.friend_id
ORDER BY u.year_of_birth ASC, u.month_of_birth ASC, u.day_of_birth ASC, f.user_id DESC
FETCH FIRST 1 ROWS ONLY;
--youngest query
SELECT u.user_id, u.first_name, u.last_name
FROM project2.Public_Users u
JOIN (
    SELECT user1_id AS friend_id FROM project2.Public_Friends WHERE user2_id = userID
    UNION
    SELECT user2_id AS friend_id FROM project2.Public_Friends WHERE user1_id = userID
) f ON u.user_id = f.friend_id
ORDER BY u.year_of_birth DESC, u.month_of_birth DESC, u.day_of_birth DESC, f.user_id DESC
FETCH FIRST 1 ROWS ONLY;
