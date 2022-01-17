package connectors;


//import com.datastax.driver.core.Cluster;
//import com.datastax.driver.core.CodecRegistry;
//import com.datastax.driver.extras.codecs.enums.EnumNameCodec;
//import com.datastax.driver.mapping.Mapper;
//import com.datastax.driver.mapping.MappingManager;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.api.querybuilder.schema.CreateKeyspace;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.datastax.oss.driver.api.querybuilder.schema.Drop;
import models.daos.*;
import models.entities.EC2Instance;
import models.mappers.InventoryMapper;
import org.javatuples.Quartet;
import org.javatuples.Quintet;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class CassandraConnector {

    private CqlSession session;

    private InventoryMapper mappingManager;

    public CassandraConnector() { }

    public void connect(String node, Integer port) {
        File config = new File(getClass().getClassLoader().getResource("datastax-java-driver.conf").getFile());

        this.session = CqlSession.builder().addContactPoint(new InetSocketAddress(node, port))
                .withLocalDatacenter("Mars")
                .withConfigLoader(DriverConfigLoader.fromFile(config))
                .build();
        this.createMappingManager("aws3");
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
        dropDatabase(keyspaceName);
        initDatabaseSchema(keyspaceName);
    }

    public void dropDatabase(String keyspaceName) {
        Drop dropSt = SchemaBuilder.dropTable(keyspaceName, "availability_zone").ifExists();
        session.executeAsync(dropSt.build()).toCompletableFuture();

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

    public void initDatabaseSchema(String keyspaceName) {
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
                .withClusteringColumn("user_name", DataTypes.TEXT);
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
                .withPartitionKey("az_name", DataTypes.TEXT)
                .withColumn("spots_reserved", DataTypes.COUNTER);
        session.execute(createTableSt.build());

        createTableSt = SchemaBuilder.createTable(keyspaceName, "user").ifNotExists()
                .withPartitionKey("name", DataTypes.TEXT)
                .withColumn("password", DataTypes.TEXT)
                .withColumn("credits", DataTypes.DECIMAL);
        session.execute(createTableSt.build());
    }

    public void insertSampleData() {
        Ec2InstanceDao ec2InstanceDao = this.mappingManager.ec2InstanceDao();
        UserDao userDao = this.mappingManager.userDao();
        AZToEc2MappingDao azToEc2MappingDao = this.mappingManager.azToEc2MappingDao();
        AWSSpotDao awsSpotDao = this.mappingManager.awsSpotDao();
        AvailabilityZoneDao availabilityZoneDao = this.mappingManager.availabilityZoneDao();
        SpotsReservedDao spotsReservedDao = this.mappingManager.spotsReservedDao();

        ArrayList<Quintet<String, String, Integer, Integer, String>> ec2InstancesParams = new ArrayList(List.of(
                new Quintet("t1.micro", "t1", 1, 626, "Very Low"),
                new Quintet("t2.micro", "t2", 1, 1024, "Low to moderate"),
                new Quintet("t2.nano", "t2", 1, 512, "Low to moderate"),
                new Quintet("t2.small", "t2", 1, 2048, "Low to moderate"),
                new Quintet("t2.medium", "t2", 2, 4096, "Low to moderate"),
                new Quintet("t2.large", "t2", 2, 8192, "Low to moderate"),
                new Quintet("t3.nano", "t3", 2, 512, "Up to 5 Gigabit"),
                new Quintet("t3.micro", "t3", 2, 1024, "Up to 5 Gigabit")
        ));
        for (Quintet<String, String, Integer, Integer, String> ec2InstanceParams: ec2InstancesParams) {
            EC2Instance ec2Instance = new EC2Instance(ec2InstanceParams.getValue0(),
                    ec2InstanceParams.getValue1(),
                    ec2InstanceParams.getValue2(),
                    ec2InstanceParams.getValue3(),
                    ec2InstanceParams.getValue4());
            ec2InstanceDao.save(ec2Instance);
        }

        // TODO: Add data for remaining tables
    }



    /*
     * ascii      boolean    decimal    float      int        set        time       tinyint    varint
     * bigint     counter    double     frozen     list       smallint   timestamp  uuid
     * blob       date       duration   inet       map        text       timeuuid   varchar
     */
}
