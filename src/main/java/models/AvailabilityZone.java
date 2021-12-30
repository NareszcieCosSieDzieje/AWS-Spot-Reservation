package models;

import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;

import java.util.List;

/*
 * ascii      boolean    decimal    float      int        set        time       tinyint    varint
 * bigint     counter    double     frozen     list       smallint   timestamp  uuid
 * blob       date       duration   inet       map        text       timeuuid   varchar
 * Table AvailabilityZone
 *   region text, PK # US EAST
 *   name text, PART # 1a
 *   available_spots ONE_TO_MANY FK -> AZToEC2Mapping
 *   status text # up, down
 *
 */
@Table(keyspace = "aws", name = "availability_zone",
        caseSensitiveKeyspace = false,
        caseSensitiveTable = false)
public class AvailabilityZone {
    @PartitionKey(0)
    private String region;
    @PartitionKey(1)
    private String name;
    private List<AZToEC2Mapping> available_spots;
//    @Enumerated(EnumType.STRING)
//    private AZStatus status;
}
