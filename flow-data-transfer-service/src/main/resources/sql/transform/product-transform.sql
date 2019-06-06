UPDATE transform_execution_log
SET    current_execution_timestamp=CURRENT_TIMESTAMP;

INSERT INTO flow_cooked.traget_table_1
SELECT source_table_1.id,
       source_table_1.value1
              ||'-'
              ||source_table_2.value3,
       CURRENT_TIMESTAMP
FROM   source_table_1
JOIN   source_table_2
ON     (
              source_table_1.id_table_2=source_table_2.id)
WHERE  source_table_1.last_update_timestamp >
       (
              SELECT last_execution_timestamp
              FROM   transform_execution_log
              WHERE  transform_name='product-transform')
on conflict (id) do
UPDATE
SET    value4=excluded.value4,
       last_update_timestamp=excluded.last_update_timestamp;

UPDATE transform_execution_log
SET    last_execution_timestamp=current_execution_timestamp;
