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

//@Table(keyspace = "aws", name = "az_to_ec2_mapping",
//        caseSensitiveKeyspace = false,
@Entity(defaultKeyspace = "aws")
@CqlName(value = "az_to_ec2_mapping")
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

