package connectors;


//import com.datastax.driver.core.Cluster;
//import com.datastax.driver.core.CodecRegistry;
//import com.datastax.driver.extras.codecs.enums.EnumNameCodec;
//import com.datastax.driver.mapping.Mapper;
//import com.datastax.driver.mapping.MappingManager;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.api.querybuilder.schema.CreateKeyspace;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.datastax.oss.driver.api.querybuilder.schema.Drop;
import models.mappers.InventoryMapper;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class CassandraConnector {

//    private Cluster cluster;

    private CqlSession session;

    public InventoryMapper mappingManager; // fixme private

//    private HashMap<String, Mapper> modelMapping = new HashMap<String, Mapper>();
    private HashMap<String, Object> modelMapping = new HashMap<String, Object>();

    public CassandraConnector() {

    }

    public void connect(String node, Integer port) {
        this.session = CqlSession.builder().addContactPoint(new InetSocketAddress(node, port)).withLocalDatacenter("Mars").build();
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

    public void createMappingManager(String defaultKeySpace) {
        if (this.session != null) {
            this.mappingManager = InventoryMapper.builder(this.session)
                    .withDefaultKeyspace(defaultKeySpace)
                    .build(); // creates mapping
        }
    }

//    public void createObjectMapping() {
//
//        if (this.mappingManager != null) {
//            Mapper<AvailabilityZone> mapper = this.mappingManager.mapper(AvailabilityZone.class);
//            this.modelMapping.put("availabilty_zone", mapper);
//            Mapper<AWSSpot> mapper = this.mappingManager.mapper(AWSSpot.class);
//            this.modelMapping.put("aws_spot", mapper);
//            Mapper<EC2Instance> mapper = this.mappingManager.mapper(EC2Instance.class);
//            this.modelMapping.put("ec2_instance", mapper);
//            this.modelMapping.put("az_to_ec2_mapping", mapper);
//            Mapper<AvailabilityZone> mapper = this.mappingManager.mapper(AvailabilityZone.class);
//            this.modelMapping.put("spots_reserved", mapper);
//        }
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

    public void rebuildDatabase(String keyspaceName) {
        cleanDatabase(keyspaceName);
        initDatabase(keyspaceName);
    }

    public void cleanDatabase(String keyspaceName) {
        Drop dropSt = SchemaBuilder.dropTable(keyspaceName, "availability_zone").ifExists();
        session.execute(dropSt.build());

        dropSt = SchemaBuilder.dropTable(keyspaceName, "ec2_instance").ifExists();
        session.execute(dropSt.build());

        dropSt = SchemaBuilder.dropTable(keyspaceName, "aws_spot").ifExists();
        session.execute(dropSt.build());

        dropSt = SchemaBuilder.dropTable(keyspaceName, "az_to_ec2_mapping").ifExists();
        session.execute(dropSt.build());

        dropSt = SchemaBuilder.dropTable(keyspaceName, "spots_reserved").ifExists();
        session.execute(dropSt.build());
    }

    public void initDatabase(String keyspaceName) {
        CreateTable createTableSt = SchemaBuilder.createTable(keyspaceName, "availability_zone").ifNotExists()
                .withPartitionKey("region", DataTypes.TEXT)
                .withClusteringColumn("name", DataTypes.TEXT)
                .withColumn("status", DataTypes.TEXT);
        session.execute(createTableSt.build());

        createTableSt = SchemaBuilder.createTable(keyspaceName, "ec2_instance").ifNotExists()
                .withPartitionKey("instance_type", DataTypes.ASCII)
                .withClusteringColumn("family", DataTypes.ASCII)
                .withColumn("vcpu_cores", DataTypes.INT)
                .withColumn("memory_size", DataTypes.INT)
                .withColumn("network_performance", DataTypes.TEXT);
        session.execute(createTableSt.build());

        createTableSt = SchemaBuilder.createTable(keyspaceName, "aws_spot").ifNotExists()
                .withPartitionKey("region", DataTypes.TEXT)
                .withPartitionKey("az_name", DataTypes.TEXT)
                .withPartitionKey("instance_type", DataTypes.ASCII)
                .withClusteringColumn("max_price", DataTypes.DECIMAL);
        session.execute(createTableSt.build());

        createTableSt = SchemaBuilder.createTable(keyspaceName, "az_to_ec2_mapping").ifNotExists()
                .withPartitionKey("region", DataTypes.TEXT)
                .withPartitionKey("instance_type", DataTypes.ASCII)
                .withClusteringColumn("az_name", DataTypes.TEXT)
                .withColumn("min_price", DataTypes.DECIMAL)
                .withColumn("current_price", DataTypes.DECIMAL)
                .withColumn("max_spots_available", DataTypes.INT);
        session.execute(createTableSt.build());

        createTableSt = SchemaBuilder.createTable(keyspaceName, "spots_reserved").ifNotExists()
                .withPartitionKey("region", DataTypes.TEXT)
                .withPartitionKey("instance_type", DataTypes.ASCII)
                .withClusteringColumn("az_name", DataTypes.TEXT)
                .withColumn("spots_reserved", DataTypes.COUNTER);
        session.execute(createTableSt.build());
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
