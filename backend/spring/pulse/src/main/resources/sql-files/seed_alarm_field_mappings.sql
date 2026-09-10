-- Seed Alarm Sources:

INSERT INTO alarm_sources (name,
                           label,
                           created_at,
                           last_modified_at,
                           created_by,
                           last_modified_by)
VALUES ('ume',
        'UME',
        CURRENT_TIMESTAMP,
        NULL,
        'system',
        NULL),
       ('u31',
        'U31',
        CURRENT_TIMESTAMP,
        NULL,
        'system',
        NULL),
       ('u2020',
        'U2020',
        CURRENT_TIMESTAMP,
        NULL,
        'system',
        NULL);


--Seed: UME - ZTE

INSERT INTO alarm_field_mappings (alarm_source_id, canonical_field, source_column, is_required)
SELECT id, v.canonical_field, v.source_column, v.is_required
FROM alarm_sources,
     (VALUES ('NODE_NAME', 'ME', true),
             ('ALARM_CODE', 'Alarm Code', true),
             ('ALARM_NAME', 'Alarm Code Name', true),
             ('OCCURRENCE_TIME', 'Occurrence Time', true),
             ('SPECIFIC_PROBLEM', 'Specific Problem', false),
             ('SEVERITY', 'Alarm Severity', true),
             ('ACK_STATE', 'Ack State', true),
             ('ALARM_ID', 'Alarm ID', true),
             ('ALARM_TYPE', 'Alarm Type', true),
             ('LOCATION', 'Location', false),
             ('ADDITIONAL_INFO', 'Additional Information', false),
             ('DESCRIPTION', 'DN', false),
             ('CLEAR_STATE', 'Clear State', true),
             ('CLEAR_TIME', NULL, false)) AS v(canonical_field, source_column, is_required)
WHERE alarm_sources.name = 'ume';

-- ============================================================
-- Seed: U31 - ZTE
-- "Alarm Code" cell holds BOTH the name and the code, e.g. "Board Fault(1234)".
-- ALARM_CODE extracts the digits inside parentheses.
-- ALARM_NAME extracts the text before the parenthesis.

INSERT INTO alarm_field_mappings (alarm_source_id, canonical_field, source_column, extraction_regex, default_value,
                                  is_required)
SELECT id, v.canonical_field, v.source_column, v.extraction_regex, v.default_value, v.is_required
FROM alarm_sources,
     (VALUES ('NODE_NAME', 'NE', NULL, NULL, true),
             ('ALARM_CODE', 'Alarm Code', '\((\d+)\)', NULL, true),
             ('ALARM_NAME', 'Alarm Code', '^(.*?)\s*\(', NULL, true),
             ('OCCURRENCE_TIME', 'Raised Time', NULL, NULL, true),
             ('SPECIFIC_PROBLEM', 'Specific Problem', NULL, NULL, false),
             ('SEVERITY', 'Severity', NULL, NULL, true),
             ('ACK_STATE', 'ACK State', NULL, NULL, true),
             ('ALARM_ID', 'Alarm ID', NULL, NULL, true),
             ('ALARM_TYPE', 'System Type', NULL, NULL, true),
             ('LOCATION', 'Location', NULL, NULL, false),
             ('ADDITIONAL_INFO', 'Remark', NULL, NULL, false),
             ('DESCRIPTION', 'Custom Attribute 12', NULL, NULL, false),
             ('CLEAR_STATE', NULL, NULL, 'UNCLEARED', false),
             ('CLEAR_TIME', NULL, NULL, NULL, false)) AS v(canonical_field, source_column, extraction_regex,
                                                           default_value, is_required)
WHERE alarm_sources.name = 'u31';

-- ============================================================
-- Seed: U2020

INSERT INTO alarm_field_mappings (alarm_source_id, canonical_field, source_column, default_value, is_required)
SELECT id, v.canonical_field, v.source_column, v.default_value, v.is_required
FROM alarm_sources,
     (VALUES ('NODE_NAME', 'Alarm Source', NULL, true),
             ('ALARM_CODE', 'Alarm ID', NULL, true),
             ('ALARM_NAME', 'AlarmName', NULL, true),
             ('OCCURRENCE_TIME', 'OccurrenceTime', NULL, true),
             ('SPECIFIC_PROBLEM', NULL, NULL, false),
             ('SEVERITY', 'Severity', NULL, true),
             ('ACK_STATE', 'Status', NULL, true),
             ('ALARM_ID', 'Log Serial Number', NULL, true),
             ('ALARM_TYPE', 'Type', NULL, true),
             ('LOCATION', 'Object Identity Name', NULL, false),
             ('ADDITIONAL_INFO', 'LocationInformation', NULL, false),
             ('DESCRIPTION', 'addtional Text', NULL, false),
             ('CLEAR_STATE', NULL, NULL, false), -- inferred from ClearanceTime — Python
             ('CLEAR_TIME', 'ClearanceTime', NULL,
              false)) AS v(canonical_field, source_column, default_value, is_required)
WHERE alarm_sources.name = 'u2020';

--- MODIFY

UPDATE alarm_field_mappings
SET source_column    = 'Status',
    extraction_regex = '^(\w+)'
WHERE canonical_field = 'ACK_STATE'
  AND alarm_source_id = (SELECT id FROM alarm_sources WHERE name = 'u2020');

UPDATE alarm_field_mappings
SET source_column    = 'Status',
    extraction_regex = 'and (\w+) Alarm',
    default_value    = NULL
WHERE canonical_field = 'CLEAR_STATE'
  AND alarm_source_id = (SELECT id FROM alarm_sources WHERE name = 'u2020');

INSERT INTO alarm_value_mappings (alarm_source_id, mapping_type, raw_value, canonical_value)
SELECT id, 'ACK_STATE', 'UNACK', 'UNACKNOWLEDGED'
FROM alarm_sources
WHERE name = 'ume'
UNION ALL
SELECT id, 'ACK_STATE', 'ACK', 'ACKNOWLEDGED'
FROM alarm_sources
WHERE name = 'ume';