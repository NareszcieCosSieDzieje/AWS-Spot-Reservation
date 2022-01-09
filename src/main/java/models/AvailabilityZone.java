package models;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
//import com.datastax.driver.mapping.annotations;

import java.util.List;

/*
 * ascii      boolean    decimal    float      int        set        time       tinyint    varint
 * bigint     counter    double     frozen     list       smallint   timestamp  uuid
 * blob       date       duration   inet       map        text       timeuuid   varchar
 *
 * Table AvailabilityZone
 *   region text, PK # US EAST
 *   name text, CC # 1a
 *   available_spots ONE_TO_MANY FK -> AZToEC2Mapping
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

    @Column(name = "available_spots")
    private List<AZToEC2Mapping> available_spots;

    @Column(name = "status")
    private AZStatus status;
}
