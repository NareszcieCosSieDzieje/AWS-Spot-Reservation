package client;

import com.datastax.oss.driver.api.core.PagingIterable;
import connectors.CassandraConnector;
import models.daos.AWSSpotDao;
import models.daos.Ec2InstanceDao;
import models.entities.AWSSpot;
import models.entities.EC2Instance;
import models.mappers.InventoryMapper;

/*
* AWS CLIENT Steps ->
* 1. Connect to db.
* while True:
*   2. Show available spots
*   3. Choose a spot
*   4.
* */

public class AwsClient {

    public static void main(String[] args) {

        String message = null;
        if (args.length < 2) {
            message = "Missing arguments (node_ip) | (node_port)";
            System.out.println(message);
            return;
        }

        CassandraConnector cassandraConnector = new CassandraConnector();
        cassandraConnector.connect(args[0], Integer.parseInt(args[1]));
        cassandraConnector.createKeyspace("aws3", "SimpleStrategy", 2);
        cassandraConnector.initDatabase("aws3");
        cassandraConnector.createMappingManager("aws3");

        new AwsConsoleInterface(cassandraConnector.getInventoryMapper()).startLoop();

        // TODO: Launch some kind of UI in loop
        InventoryMapper inventoryMapper = cassandraConnector.getInventoryMapper();
        Ec2InstanceDao ec2InstanceDao = inventoryMapper.ec2InstanceDao();
        ec2InstanceDao.save(new EC2Instance("t.big", "enterprise", 8, 4, "super-fast"));
        PagingIterable<EC2Instance> ec2instances = ec2InstanceDao.findAll();
        for (EC2Instance ec2instance: ec2instances) {
            System.out.println("my new sweet ec2 instance: " + ec2instance.toString());
        }

        AWSSpotDao awsSpotDao = inventoryMapper.awsSpotDao();
        PagingIterable<AWSSpot> spots = awsSpotDao.findAll();
        System.out.println(spots.one().toString());

        System.out.println("Done");
        cassandraConnector.close();
//        cassandraConnector.createKeyspace("aws", "SimpleStrategy", 2);
//        cassandraConnector.initCleanDatabase("aws");
////        cassandraConnector.close();
//        Session session = cassandraConnector.getSession();
////        for (Row r: session.execute("SELECT * FROM test.mytable")) {
////            System.out.println(r.toString());
////        };
//        System.out.println(session);
//        cassandraConnector.close();
    }
}
