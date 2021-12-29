package client;
import connectors.CassandraConnector;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Row;

public class AwsClient {

    public static void main(String[] args) {

        if (args.length < 2) {
            String message = "Missing arguments (node_ip) | (node_port)";
            System.out.println(message);
        }

        CassandraConnector cassandraConnector = new CassandraConnector();
        cassandraConnector.connect(args[0], Integer.parseInt(args[1]));
//        cassandraConnector.close();
        Session session = cassandraConnector.getSession();
//        for (Row r: session.execute("SELECT * FROM test.mytable")) {
//            System.out.println(r.toString());
//        };
        System.out.println(session);
        cassandraConnector.close();
    }
}