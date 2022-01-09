package models;

/*
 * ascii      boolean    decimal    float      int        set        time       tinyint    varint
 * bigint     counter    double     frozen     list       smallint   timestamp  uuid
 * blob       date       duration   inet       map        text       timeuuid   varchar
 *
 * Table AWSSpot
 *   region text, PK FK -> AvailabilityZone
 *   az_name text, PK FK -> AvailabilityZone
 *   instance_type ascii PK FK -> EC2Instance(instance_type)
 *   max_price decimal, CC # how much are you willing to pay to keep running instance
 */

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

@Table(keyspace = "aws", name = "aws_spot",
        caseSensitiveKeyspace = false,
        caseSensitiveTable = false)
public class AWSSpot {
    @PartitionKey(0)
    @Column(name = "region")
    private String region;

    @PartitionKey(1)
    @Column(name = "az_name")
    private String az_name;

    @PartitionKey(2)
    @Column(name = "instance_type")
    private String instance_type; //fixme ascii

    @ClusteringColumn
    @Column(name = "max_price")
    private Float max_price;
}
