
UPDATE granularity
SET window_seconds = 86400
WHERE name = 'day-average';
UPDATE granularity
SET window_seconds = 3600
WHERE name = 'busy-hour';