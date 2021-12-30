package models;

/*
 * ascii      boolean    decimal    float      int        set        time       tinyint    varint
 * bigint     counter    double     frozen     list       smallint   timestamp  uuid
 * blob       date       duration   inet       map        text       timeuuid   varchar
 * Table AZToEC2Mapping
 *   region text, FK -> AvailabilityZone PK
 *   instance_type ascii, FK -> EC2Instance PK
 *   az_name text, FK -> AvailabilityZone PART
 *   min_price decimal, # CONSTANT
 *   current_price decimal, # BASED ON spots_reserved and instance type
 *   max_spots_available int,
 *   spots_reserved counter
 */

public class AZToEC2Mapping {
}
