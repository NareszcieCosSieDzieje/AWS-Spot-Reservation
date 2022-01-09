package models;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

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

//@Table(keyspace = "aws", name = "az_to_ec2_mapping",
//        caseSensitiveKeyspace = false,
@Entity(defaultKeyspace = "aws")
@CqlName(value = "az_to_ec2_mapping")
public class AZToEC2Mapping {
    @PartitionKey(0)
    @CqlName(value = "region")
    private String region;

    @PartitionKey(1)
    @CqlName(value = "instance_type")
    private String instance_type;

    @ClusteringColumn
    @CqlName(value = "az_name")
    private String az_name;

    @CqlName(value = "min_price")
    private float min_price;

    @CqlName(value = "current_price")
    private float current_price;

    @CqlName(value = "max_spots_available")
    private int max_spots_available;

    @CqlName(value = "spots_reserved")
    private int spots_reserved;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getInstance_type() {
        return instance_type;
    }

    public void setInstance_type(String instance_type) {
        this.instance_type = instance_type;
    }

    public String getAz_name() {
        return az_name;
    }

    public void setAz_name(String az_name) {
        this.az_name = az_name;
    }

    public float getMin_price() {
        return min_price;
    }

    public void setMin_price(float min_price) {
        this.min_price = min_price;
    }

    public float getCurrent_price() {
        return current_price;
    }

    public void setCurrent_price(float current_price) {
        this.current_price = current_price;
    }

    public int getMax_spots_available() {
        return max_spots_available;
    }

    public void setMax_spots_available(int max_spots_available) {
        this.max_spots_available = max_spots_available;
    }

    public int getSpots_reserved() {
        return spots_reserved;
    }

    public void setSpots_reserved(int spots_reserved) {
        this.spots_reserved = spots_reserved;
    }
}

