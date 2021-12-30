package connectors;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class CassandraConnector {

    private Cluster cluster;

    private Session session;

    public CassandraConnector() {

    }

    public void connect(String node, Integer port) {
        Cluster.Builder b = Cluster.builder().addContactPoint(node);
        if (port != null) {
            b.withPort(port);
        }
        cluster = b.build();

        session = cluster.connect();
    }

    public Session getSession() {
        return this.session;
    }

    public void close() {
        session.close();
        cluster.close();
    }

    public void createKeyspace(
            String keyspaceName, String replicationStrategy, int replicationFactor) {
        String query = "CREATE KEYSPACE IF NOT EXISTS " +
                keyspaceName + " WITH replication = {" +
                "'class':'" + replicationStrategy +
                "','replication_factor':" + replicationFactor +
                "};";
        session.execute(query);
    }

    public void initCleanDatabase(String keyspaceName) {
        final String dropAvailabilityZone = "DROP TABLE IF EXISTS " + keyspaceName + ".AvailabilityZone;";
        final String dropEC2Instance = "DROP TABLE IF EXISTS " + keyspaceName + ".EC2Instance;";
        final String dropAWSSpot = "DROP TABLE IF EXISTS " + keyspaceName + ".AWSSpot;";
        final String dropAZToEC2Mapping = "DROP TABLE IF EXISTS " + keyspaceName + ".AZToEC2Mapping;";

        final String createAvailabilityZone = "CREATE TABLE  " + keyspaceName + ".AvailabilityZone(region text, name text, status text, available_spots list, PRIMARY KEY((region), name));";
        final String createEC2Instance = "CREATE TABLE  " + keyspaceName + ".EC2Instance(family ascii, instance_type ascii, vcpu_cores int, memory_size int, network_performance text, PRIMARY KEY((instance_type), family));";
        final String createAWSSpot = "CREATE TABLE  " + keyspaceName + ".AWSSpot(region text, az_name text, instance_type ascii, max_price decimal, PRIMARY KEY((region, az_name, instance_type), max_price);";
        final String createAZToEC2Mapping = "CREATE TABLE  " + keyspaceName + ".AZToEC2Mapping(region text, instance_type ascii, az_name text, min_price decimal, current_price decimal, max_spots_available int, spots_reserved counter, PRIMARY KEY((region, instance_type), az_name));";

        session.execute(dropAWSSpot);
        session.execute(dropAZToEC2Mapping);
        session.execute(dropAvailabilityZone);
        session.execute(dropEC2Instance);

        session.execute(createAvailabilityZone);
        session.execute(createEC2Instance);
        session.execute(createAWSSpot);
        session.execute(createAZToEC2Mapping);
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
     * Table AvailabilityZone
     *   region text, PK # US EAST
     *   name text, CC # 1a
     *   available_spots ONE_TO_MANY FK -> AZToEC2Mapping
     *   status text # up, down
     *
     */
}
