--replace YEARDIFF with java variable
SELECT u1.user_id, u1.first_name, u1.last_name,u2.user_id, u2.first_name, u2.last_name
FROM project2.Public_Users u1
JOIN project2.Public_Users u2 ON u1.gender = u2.gender AND u1.user_id < u2.user_id
JOIN project2.Public_Tags t1 ON u1.user_id = t1.tag_subject_id
JOIN project2.Public_Tags t2 ON u2.user_id = t2.tag_subject_id AND t1.tag_photo_id = t2.tag_photo_id
WHERE ABS(u1.year_of_birth - u2.year_of_birth) <= YEARDIFF
AND NOT EXISTS (
    SELECT 1
    FROM project2.Public_Friends f
    WHERE f.user1_id = u1.user_id AND f.user2_id = u2.user_id
)
GROUP BY u1.user_id, u1.first_name, u1.last_name, u2.user_id, u2.first_name, u2.last_name
ORDER BY COUNT(*) DESC, u1.user_id ASC, u2.user_id ASC;
--replace U1 and U2 with java variables where U1 < U2
SELECT t1.tag_photo_id, ph.photo_link, ph.album_id, al.album_name
FROM project2.Public_Tags t1
JOIN project2.Public_Tags t2 ON t1.tag_photo_id = t2.tag_photo_id
JOIN project2.Public_Photos ph ON t1.tag_photo_id = ph.photo_id
JOIN project2.Public_Albums al ON ph.album_id = al.album_id
WHERE t1.tag_subject_id = U1 AND t2.tag_subject_id = U2
ORDER BY t1.tag_photo_id ASC;




