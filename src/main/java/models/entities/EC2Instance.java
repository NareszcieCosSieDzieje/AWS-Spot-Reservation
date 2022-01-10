package models.entities;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

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


@Entity(defaultKeyspace = "aws")
@CqlName(value = "ec2_instance")
public class EC2Instance {
    @PartitionKey
    @CqlName(value = "instance_type")
    private String instance_type;

    @ClusteringColumn
    @CqlName(value = "family")
    private String family;

    @CqlName(value = "vcpu_cores")
    private int vcpu_cores;

    @CqlName(value = "memory_size")
    private int memory_size;

    @CqlName(value = "network_performance")
    private String network_performance;

    public EC2Instance() {

    }

    public EC2Instance(String instance_type, String family, int vcpu_cores, int memory_size, String network_performance) {
        this.instance_type = instance_type;
        this.family = family;
        this.vcpu_cores = vcpu_cores;
        this.memory_size = memory_size;
        this.network_performance = network_performance;
    }

    public String getInstance_type() {
        return instance_type;
    }

    public void setInstance_type(String instance_type) {
        this.instance_type = instance_type;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public int getVcpu_cores() {
        return vcpu_cores;
    }

    public void setVcpu_cores(int vcpu_cores) {
        this.vcpu_cores = vcpu_cores;
    }

    public int getMemory_size() {
        return memory_size;
    }

    public void setMemory_size(int memory_size) {
        this.memory_size = memory_size;
    }

    public String getNetwork_performance() {
        return network_performance;
    }

    public void setNetwork_performance(String network_performance) {
        this.network_performance = network_performance;
    }

    @Override
    public String toString() {
        return "EC2Instance{" +
                "instance_type='" + instance_type + '\'' +
                ", family='" + family + '\'' +
                ", vcpu_cores=" + vcpu_cores +
                ", memory_size=" + memory_size +
                ", network_performance='" + network_performance + '\'' +
                '}';
    }
}
