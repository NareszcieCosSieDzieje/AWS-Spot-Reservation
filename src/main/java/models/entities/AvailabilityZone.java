package models.entities;


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

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import models.enums.AZStatus;


//@Table(keyspace = "aws", name = "availability_zone",
//        caseSensitiveKeyspace = false,
//        caseSensitiveTable = false)
@Entity(defaultKeyspace = "aws")
@CqlName(value = "availability_zone")
public class AvailabilityZone {

    @PartitionKey
    @CqlName("region")
    private String region;

    @ClusteringColumn
    @CqlName("name")
    private String name;

    @CqlName("status")
    private AZStatus status;

    public AvailabilityZone() {

    }

    public AvailabilityZone(String region, String name, AZStatus status) {
        this.region = region;
        this.name = name;
        this.status = status;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AZStatus getStatus() {
        return status;
    }

    public void setStatus(AZStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "AvailabilityZone{" +
                "region='" + region + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}
