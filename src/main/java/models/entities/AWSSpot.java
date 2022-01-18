package models.entities;

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

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.UUID;

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
    private String instance_type;

    @PartitionKey(3)
    @CqlName(value = "spot_id")
    private UUID spot_id;

    @ClusteringColumn
    @CqlName(value = "max_price")
    private BigDecimal max_price;

    @CqlName(value = "user_name")
    private String user_name;

    public AWSSpot() {

    }

    public AWSSpot(String region, String az_name, String instance_type, BigDecimal max_price, String user_name) {
        this.region = region;
        this.az_name = az_name;
        this.instance_type = instance_type;
        this.spot_id = UUID.randomUUID();
        this.max_price = max_price;
        this.user_name = user_name;
    }

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

    public UUID getSpot_id() {
        return spot_id;
    }

    public void setSpot_id(UUID spot_id) {
        this.spot_id = spot_id;
    }

    public BigDecimal getMax_price() {
        return max_price;
    }

    public void setMax_price(BigDecimal max_price) {
        this.max_price = max_price;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public static Comparator<AWSSpot> sortByMaxPrice = new Comparator<AWSSpot>() {
        @Override
        public int compare(AWSSpot spot1, AWSSpot spot2) {
            //sort in ascending order
            return spot1.getMax_price().compareTo(spot2.getMax_price());
        }
    };

    @Override
    public String toString() {
        return "AWSSpot{" +
                "region='" + region + '\'' +
                ", az_name='" + az_name + '\'' +
                ", instance_type='" + instance_type + '\'' +
                ", spot_id='" + spot_id + '\'' +
                ", max_price=" + max_price +
                ", user_name=" + user_name +
                '}';
    }
}
