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

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

//@Entity(keyspace = "aws", name = "aws_spot",
//        caseSensitiveKeyspace = false,
//        caseSensitiveTable = false)
@Entity(defaultKeyspace = "aws")
@CqlName(value = "aws_spot")
public class AWSSpot {

    @PartitionKey
    @CqlName(value = "region")
    private String region;

    @PartitionKey(1)
    @CqlName(value = "az_name")
    private String az_name;

    @PartitionKey(2)
    @CqlName(value = "instance_type")
    private String instance_type; //fixme ascii

    @ClusteringColumn
    @CqlName(value = "max_price")
    private Float max_price;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAz_name() {
        return az_name;
    }

    public void setAz_name(String az_name) {
        this.az_name = az_name;
    }

    public String getInstance_type() {
        return instance_type;
    }

    public void setInstance_type(String instance_type) {
        this.instance_type = instance_type;
    }

    public Float getMax_price() {
        return max_price;
    }

    public void setMax_price(Float max_price) {
        this.max_price = max_price;
    }
}
