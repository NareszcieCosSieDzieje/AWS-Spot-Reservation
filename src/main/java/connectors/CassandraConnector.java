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

    private CqlSession session;

    private InventoryMapper mappingManager;

    private HashMap<String, Object> modelMapping = new HashMap<String, Object>();

    public CassandraConnector() { }

    public void connect(String node, Integer port) {
        this.session = CqlSession.builder().addContactPoint(new InetSocketAddress(node, port))
                .withLocalDatacenter("Mars").build();
    }

    public void createMappingManager(String defaultKeySpace) {
        if (this.session != null) {
            this.mappingManager = InventoryMapper.builder(this.session)
                    .withDefaultKeyspace(defaultKeySpace)
                    .build(); // creates mapping
        }
    }

    public Session getSession() {
        return this.session;
    }

    public InventoryMapper getInventoryMapper() {
        return this.mappingManager;
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

        dropSt = SchemaBuilder.dropTable(keyspaceName, "user").ifExists();
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
                .withClusteringColumn("max_price", DataTypes.DECIMAL)
                .withClusteringColumn("user_id", DataTypes.UUID);
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

        createTableSt = SchemaBuilder.createTable(keyspaceName, "user").ifNotExists()
                .withPartitionKey("id", DataTypes.UUID)
                .withPartitionKey("name", DataTypes.TEXT)
                .withClusteringColumn("password", DataTypes.TEXT)
                .withColumn("credits", DataTypes.DECIMAL);
        session.execute(createTableSt.build());
    }

    /*
     * ascii      boolean    decimal    float      int        set        time       tinyint    varint
     * bigint     counter    double     frozen     list       smallint   timestamp  uuid
     * blob       date       duration   inet       map        text       timeuuid   varchar
     */
}
