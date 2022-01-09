package connectors;


//import com.datastax.driver.core.Cluster;
//import com.datastax.driver.core.CodecRegistry;
//import com.datastax.driver.extras.codecs.enums.EnumNameCodec;
//import com.datastax.driver.mapping.Mapper;
//import com.datastax.driver.mapping.MappingManager;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.api.querybuilder.schema.CreateKeyspace;
import models.AWSSpot;
import models.AZStatus;
import models.AvailabilityZone;
import models.EC2Instance;

import javax.xml.validation.Schema;
import java.net.InetSocketAddress;

import java.util.HashMap;

public class CassandraConnector {

//    private Cluster cluster;

    private Session session;

    public CassandraConnector() {

    }

    public void connect(String node, Integer port) {
        session = CqlSession.builder().addContactPoint(new InetSocketAddress(node, port)).build();
//        Cluster.Builder b = Cluster.builder().addContactPoint(node);
//        if (port != null) {
//            b.withPort(port);
//        }
//        cluster = b.build();
//        this.registerCodecs(cluster);
//        session = cluster.connect();
    }

//    public void registerCodecs(Cluster cluster) {
//        CodecRegistry myCodecRegistry = cluster.getConfiguration().getCodecRegistry();
//        myCodecRegistry.register(new EnumNameCodec<AZStatus>(AZStatus.class));
//    }

    public Session getSession() {
        return this.session;
    }

    public void close() {
        session.close();
    }

    public void createKeyspace(String keyspaceName, String replicationStrategy, int replicationFactor) {
        CreateKeyspace createKs = SchemaBuilder.createKeyspace(keyspaceName).ifNotExists().withSimpleStrategy(2);
        session.execute(createKs.build());
    }

    public void initDatabase(String keyspaceName) {
        SchemaBuilder.createTable("availability_zone").ifNotExists()
                .withPartitionKey("region", DataTypes.TEXT)
                .withClusteringColumn("name", DataTypes.TEXT)
                .withColumn("status", DataTypes.TEXT);

        final String dropAvailabilityZone = "DROP TABLE IF EXISTS " + keyspaceName + ".availability_zone;";
        final String dropEC2Instance = "DROP TABLE IF EXISTS " + keyspaceName + ".ec2_instance;";
        final String dropAWSSpot = "DROP TABLE IF EXISTS " + keyspaceName + ".aws_spot;";
        final String dropAZToEC2Mapping = "DROP TABLE IF EXISTS " + keyspaceName + ".az_to_ec2_mapping;";
        final String dropSpotsReserved = "DROP TABLE IF EXISTS " + keyspaceName + ".spots_reserved;";

        final String createAvailabilityZone = "CREATE TABLE  " + keyspaceName + ".availability_zone(region text, name text, status text, PRIMARY KEY((region), name));";
        final String createEC2Instance = "CREATE TABLE  " + keyspaceName + ".ec2_instance(family ascii, instance_type ascii, vcpu_cores int, memory_size int, network_performance text, PRIMARY KEY((instance_type), family));";
        final String createAWSSpot = "CREATE TABLE  " + keyspaceName + ".aws_spot(region text, az_name text, instance_type ascii, max_price decimal, PRIMARY KEY((region, az_name, instance_type), max_price));";
        final String createAZToEC2Mapping = "CREATE TABLE  " + keyspaceName + ".az_to_ec2_mapping(region text, instance_type ascii, az_name text, min_price decimal, current_price decimal, max_spots_available int, PRIMARY KEY((region, instance_type), az_name));";
        final String createSpotsReserved = "CREATE TABLE  " + keyspaceName + ".spots_reserved(region text, instance_type ascii, az_name text, spots_reserved counter, PRIMARY KEY((region, instance_type), az_name));";

        session.execute(dropAWSSpot);
        session.execute(dropAZToEC2Mapping);
        session.execute(dropAvailabilityZone);
        session.execute(dropEC2Instance);
        session.execute(dropSpotsReserved);

        session.execute(createAvailabilityZone);
        session.execute(createEC2Instance);
        session.execute(createAWSSpot);
        session.execute(createAZToEC2Mapping);
        session.execute(createSpotsReserved);
    }

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
     *
     * Table EC2Instance
     *   instance_type ascii, PK # t2.micro, t2.small
     *   family ascii, CC # t2
     *   vcpu_cores int,
     *   memory_size int,
     *   network_performance text
     *
     * Table AZToEC2Mapping
     *   region text, FK -> AvailabilityZone PK
     *   instance_type ascii, FK -> EC2Instance PK
     *   az_name text, FK -> AvailabilityZone CC
     *   min_price decimal, # CONSTANT
     *   current_price decimal, # BASED ON spots_reserved and instance type
     *   max_spots_available int,
     *   spots_reserved counter
     *
     * Table SpotsReserved
     *   region text, PK
     *   instance_type ascii, PK
     *   az_name text PK
     *   spots_reserved counter
     *
     * Table AvailabilityZone
     *   region text, PK # US EAST
     *   name text, CC # 1a
     *   -- available_spots ONE_TO_MANY FK -> AZToEC2Mapping
     *   status text # up, down
     *
     */
}
