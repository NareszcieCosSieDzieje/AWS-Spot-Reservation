package models.entities;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
/*
* Table SpotsReserved
        *   region text, PK
        *   instance_type ascii, PK
        *   az_name text PK
        *   spots_reserved counter
*/


@Entity(defaultKeyspace = "aws")
@CqlName(value = "spots_reserved")
public class SpotsReserved {
    @PartitionKey
    @CqlName(value = "region")
    private String region;

    @PartitionKey(1)
    @CqlName(value = "instance_type")
    private String instance_type;

    @PartitionKey(2)
    @CqlName(value = "az_name")
    private String az_name;

    @CqlName(value = "counter")
    private int spots_reserved;

    public SpotsReserved() {

    }

    public SpotsReserved(String region, String instance_type, String az_name, int spots_reserved) {
        this.region = region;
        this.instance_type = instance_type;
        this.az_name = az_name;
        this.spots_reserved = spots_reserved;
    }

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

    public int getSpots_reserved() {
        return spots_reserved;
    }

    public void setSpots_reserved(int spots_reserved) {
        this.spots_reserved = spots_reserved;
    }

    @Override
    public String toString() {
        return "SpotsReserved{" +
                "region='" + region + '\'' +
                ", instance_type='" + instance_type + '\'' +
                ", az_name='" + az_name + '\'' +
                ", spots_reserved=" + spots_reserved +
                '}';
    }
}
