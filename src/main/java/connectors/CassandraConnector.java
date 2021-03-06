package connectors;

import client.security.SecurePassword;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.oss.driver.api.querybuilder.schema.CreateKeyspace;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.datastax.oss.driver.api.querybuilder.schema.Drop;
import models.daos.*;
import models.entities.*;
import models.enums.AZStatus;
import models.mappers.InventoryMapper;
import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Sextet;
import org.javatuples.Triplet;

import java.io.File;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class CassandraConnector {

    private CqlSession session;

    private InventoryMapper mappingManager;

    public CassandraConnector() { }

    public void connect(String node, Integer port) {
        File config = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("datastax-java-driver.conf")).getFile());

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
                .withPartitionKey("spot_id", DataTypes.UUID)
                .withColumn("max_price", DataTypes.DECIMAL)
                .withColumn("user_name", DataTypes.TEXT);
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
                .withColumn("salt", DataTypes.TEXT)
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
                new Quintet<>("t1.micro", "t1", 1, 626, "Very Low"),
                new Quintet<>("t2.micro", "t2", 1, 1024, "Low to moderate"),
                new Quintet<>("t2.nano", "t2", 1, 512, "Low to moderate"),
                new Quintet<>("t2.small", "t2", 1, 2048, "Low to moderate"),
                new Quintet<>("t2.medium", "t2", 2, 4096, "Low to moderate"),
                new Quintet<>("t2.large", "t2", 2, 8192, "Low to moderate"),
                new Quintet<>("t3.nano", "t3", 2, 512, "Up to 5 Gigabit"),
                new Quintet<>("t3.micro", "t3", 2, 1024, "Up to 5 Gigabit")
        ));
        for (Quintet<String, String, Integer, Integer, String> ec2InstanceParams: ec2InstancesParams) {
            EC2Instance ec2Instance = new EC2Instance(ec2InstanceParams.getValue0(),
                    ec2InstanceParams.getValue1(),
                    ec2InstanceParams.getValue2(),
                    ec2InstanceParams.getValue3(),
                    ec2InstanceParams.getValue4());
            ec2InstanceDao.save(ec2Instance);
        }

        ArrayList<Triplet<String, String, BigDecimal>> usersParamsInitial = new ArrayList<>(List.of(
                new Triplet<>("Paul", "paulos123", new BigDecimal(1000)),
                new Triplet<>("Chris", "chris!", new BigDecimal(1000)),
                new Triplet<>("Adminos", "Maximos!@#", new BigDecimal(9999999)),
                new Triplet<>("JeffB", "Amazong", new BigDecimal(999999)),
                new Triplet<>("Martha", "fl0W3r", new BigDecimal(780)),
                new Triplet<>("Kate", "C0ff3#", new BigDecimal(2310))
        ));

        ArrayList<Quartet<String, String, String, BigDecimal>> userParams = new ArrayList<>();
        for (Triplet<String, String, BigDecimal> userData: usersParamsInitial) {
            String[] passwordData = SecurePassword.createSaltedHash(userData.getValue1());
            userParams.add(new Quartet<>(
                    userData.getValue0(),
                    passwordData[1], // PASS
                    passwordData[0], // SALT
                    userData.getValue2()
                    ));
        }

        for (Quartet<String, String, String, BigDecimal> userData: userParams) {
            User user = new User(userData.getValue0(),
                    userData.getValue1(),
                    userData.getValue2(),
                    userData.getValue3());
            userDao.save(user);
        }

        ArrayList<String> regions = new ArrayList<>(List.of(
                "us-east-1",
                "us-east-2",
                "eu-central-1",
                "eu-west-1",
                "eu-west-2"
        ));
        ArrayList<String> azNames = new ArrayList<>(List.of(
                "a",
                "b",
                "c"
        ));
        ArrayList<Integer> sizes = new ArrayList<>(List.of(
                10,
                20,
                30
        ));
        Random random = new Random();

        ArrayList<Sextet<String, String, String, BigDecimal, BigDecimal, Integer>> azToEC2MappingsParams = new ArrayList<>();
        for (String region: regions) {
            for (Quintet<String, String, Integer, Integer, String> ec2InstanceParams : ec2InstancesParams) {
                for (String azName : azNames) {
                    AZToEC2Mapping azToEC2Mapping = new AZToEC2Mapping(
                            region,
                            ec2InstanceParams.getValue0(),
                            azName,
                            new BigDecimal("0.0001"),
                            new BigDecimal("0.0001"),
                            sizes.get(random.nextInt(sizes.size())));
                    azToEc2MappingDao.save(azToEC2Mapping);
                    azToEC2MappingsParams.add(new Sextet<>(
                            azToEC2Mapping.getRegion(),
                            azToEC2Mapping.getInstance_type(),
                            azToEC2Mapping.getAz_name(),
                            azToEC2Mapping.getMin_price(),
                            azToEC2Mapping.getCurrent_price(),
                            azToEC2Mapping.getMax_spots_available()));
                }
            }
        }

        for (Sextet<String, String, String, BigDecimal, BigDecimal, Integer> azToEC2MappingParams: azToEC2MappingsParams) {
            for (int i = 0; i < azToEC2MappingParams.getValue5(); i++) {
                AWSSpot awsSpot = new AWSSpot(azToEC2MappingParams.getValue0(),
                        azToEC2MappingParams.getValue2(),
                        azToEC2MappingParams.getValue1(),
                        azToEC2MappingParams.getValue3(),
                        "");
                awsSpotDao.save(awsSpot);
            }
        }

        ArrayList<Triplet<String, String, AZStatus>> availabilityZonesParams = new ArrayList<>(List.of(
                new Triplet<>("us-east-1", "a", AZStatus.UP),
                new Triplet<>("us-east-1", "b", AZStatus.UP),
                new Triplet<>("us-east-2", "a", AZStatus.UP),
                new Triplet<>("eu-central-1", "a", AZStatus.UP),
                new Triplet<>("eu-central-1", "b", AZStatus.UP),
                new Triplet<>("eu-west-1", "a", AZStatus.DOWN)
        ));
        for (Triplet<String, String, AZStatus> availabilityZoneParams: availabilityZonesParams) {
            AvailabilityZone availabilityZone = new AvailabilityZone(availabilityZoneParams.getValue0(),
                    availabilityZoneParams.getValue1(),
                    availabilityZoneParams.getValue2());
            availabilityZoneDao.save(availabilityZone);
        }

        for (Sextet<String, String, String, BigDecimal, BigDecimal, Integer> azToEC2MappingParams: azToEC2MappingsParams) {
            for (int i = 0; i < azToEC2MappingParams.getValue5(); i++) {
                spotsReservedDao.increment(azToEC2MappingParams.getValue0(),
                        azToEC2MappingParams.getValue1(),
                        azToEC2MappingParams.getValue2(),
                        0L);
            }
        }
    }
}
