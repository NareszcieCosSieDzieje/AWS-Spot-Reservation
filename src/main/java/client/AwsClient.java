package client;
import connectors.CassandraConnector;
import com.datastax.driver.core.Session;

public class AwsClient {

    public static void main(String[] args) {

        if (args.length < 3) {
            String message = "Missing arguments (node_ip) | (node_port)";
            System.out.println(message);
        }

        CassandraConnector cassandraConnector = new CassandraConnector();
        cassandraConnector.connect(args[1], Integer.parseInt(args[2]));
        cassandraConnector.close();
        Session session = cassandraConnector.getSession();;
        System.out.println(session);
    }

}
