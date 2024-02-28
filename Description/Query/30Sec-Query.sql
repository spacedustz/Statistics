INSERT INTO svc_30sec_stats (yyyymmdd, hhmiss, average_count, max_people_count, min_people_count, instance_name)
SELECT '20240228', '163315', COALESCE(ROUND(AVG(source.average_count), 2), 0), COALESCE(ROUND(MAX(source.max_people_count), 2), 0), COALESCE(ROUND(MIN(source.min_people_count), 2), 0), source.instance_name
FROM svc_15sec_stats as source
WHERE source.yyyymmdd = '20240228' AND source.hhmiss BETWEEN '163315' AND '163330'
GROUP BY source.instance_name