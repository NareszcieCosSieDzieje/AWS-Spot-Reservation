package models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

/*
 * ascii      boolean    decimal    float      int        set        time       tinyint    varint
 * bigint     counter    double     frozen     list       smallint   timestamp  uuid
 * blob       date       duration   inet       map        text       timeuuid   varchar
 *
 * Table EC2Instance
 *   instance_type ascii, PK # t2.micro, t2.small
 *   family ascii, CC # t2
 *   vcpu_cores int,
 *   memory_size int,
 *   network_performance text
 */


@Table(keyspace = "aws", name = "ec2_instance",
    caseSensitiveKeyspace = false,
    caseSensitiveTable = false)
public class EC2Instance {
    @PartitionKey
    @Column(name = "instance_type")
    private String instance_type;

    @ClusteringColumn
    @Column(name = "family")
    private String family;

    @Column(name = "vcpu_cores")
    private int vcpu_cores;

    @Column(name = "memory_size")
    private int memory_size;

    @Column(name = "network_performance")
    private String network_performance;
}
