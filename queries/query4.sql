--query for most tagged
SELECT t.tag_photo_id, ph.album_id, ph.photo_link, al.album_name
FROM project2.Public_Tags t
JOIN project2.Public_Photos ph ON t.tag_photo_id = ph.photo_id
JOIN project2.Public_Albums al ON ph.album_id = al.album_id
GROUP BY t.tag_photo_id, ph.album_id, ph.photo_link, al.album_name
ORDER BY COUNT(*) DESC, t.tag_photo_id ASC;
--query for getting tagged users (replace x with java)
SELECT u.user_id, u.first_name, u.last_name
FROM (
    SELECT tag_photo_id, tag_subject_id FROM project2.Public_Tags WHERE tag_photo_id = X
) t
JOIN project2.Public_Users u ON t.tag_subject_id = u.user_id
ORDER BY u.user_id ASC;