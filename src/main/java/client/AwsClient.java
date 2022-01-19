package client;

import connectors.CassandraConnector;

public class AwsClient {

    public static void main(String[] args) {
        String message;
        boolean rebuild = false;
        boolean clean = false;
        if (args.length < 2) {
            message = "Missing arguments (node_ip) | (node_port)";
            System.out.println(message);
            return;
        }
        if (args.length == 3) {
            if (args[2].equals("rebuild")) {
                rebuild = true;
            }
            else if (args[2].equals("clean")) {
                clean = true;
            }
        }

        CassandraConnector cassandraConnector = new CassandraConnector();
        cassandraConnector.connect(args[0], Integer.parseInt(args[1]));
        if (rebuild) {
            cassandraConnector.createKeyspace("aws3", "SimpleStrategy", 2);
            cassandraConnector.rebuildDatabase("aws3");
            cassandraConnector.insertSampleData();
        } else if (clean) {
            cassandraConnector.dropDatabase("aws3");
        }

        new AwsConsoleInterface(cassandraConnector.getInventoryMapper()).startLoop();

        cassandraConnector.close();
    }
}
