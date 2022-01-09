package models;


/*
 * ascii      boolean    decimal    float      int        set        time       tinyint    varint
 * bigint     counter    double     frozen     list       smallint   timestamp  uuid
 * blob       date       duration   inet       map        text       timeuuid   varchar
 *
 * Table AvailabilityZone
 *   region text, PK # US EAST
 *   name text, CC # 1a
 *   status text # up, down
 *
 */

@Table(keyspace = "aws", name = "availability_zone",
        caseSensitiveKeyspace = false,
        caseSensitiveTable = false)
public class AvailabilityZone {
    @PartitionKey
    @Column(name = "region")
    private String region;

    @ClusteringColumn
    @Column(name = "name")
    private String name;

    @Column(name = "status")
    private AZStatus status;
}
