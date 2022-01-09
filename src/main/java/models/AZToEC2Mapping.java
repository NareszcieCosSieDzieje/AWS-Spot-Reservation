package models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Table;

/*
 * ascii      boolean    decimal    float      int        set        time       tinyint    varint
 * bigint     counter    double     frozen     list       smallint   timestamp  uuid
 * blob       date       duration   inet       map        text       timeuuid   varchar
 *
 * Table AZToEC2Mapping
 *   region text, FK -> AvailabilityZone PK
 *   instance_type ascii, FK -> EC2Instance PK
 *   az_name text, FK -> AvailabilityZone CC
 *   min_price decimal, # CONSTANT
 *   current_price decimal, # BASED ON spots_reserved and instance type
 *   max_spots_available int,
 *   spots_reserved counter
 */

@Table(keyspace = "aws", name = "az_to_ec2_mapping",
        caseSensitiveKeyspace = false,
        caseSensitiveTable = false)
public class AZToEC2Mapping {
    @PartitionKey
    @Column(name = "region")
    private String region;

    @PartitionKey(1)
    @Column(name = "instance_type")
    private String instance_type;

    @ClusteringColumn
    @Column(name = "az_name")
    private String az_name;

    @Column(name = "min_price")
    private float min_price;

    @Column(name = "current_price")
    private float current_price;

    @Column(name = "max_spots_available")
    private int max_spots_available;

    @Column(name = "spots_reserved")
    private int spots_reserved;
}
