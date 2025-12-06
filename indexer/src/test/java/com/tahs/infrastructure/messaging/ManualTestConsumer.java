package com.tahs.infrastructure.messaging;

import com.tahs.domain.IngestionEvent;
import org.apache.activemq.ActiveMQConnectionFactory;
import com.google.gson.Gson;

import javax.jms.*;

/**
 * Manual test class to CONSUME messages from the 'book.events' queue.
 * Use this to verify that messages are actually arriving in the queue.
 *
 * NOTE: If your main Indexer application is running, it might compete with this
 * consumer for messages! Stop the Indexer app to guarantee this script gets the
 * message.
 */
public class ManualTestConsumer {

    // Ensure this matches your broker URL.
    // Even if you omit one node, the cluster topology might discover it.
    private static final String BROKER_URL = "failover:(tcp://192.168.1.133:61616,tcp://192.168.1.135:61616,tcp://192.168.1.143:61616)?randomize=false";
    private static final String QUEUE_NAME = "book.events";

    public static void main(String[] args) {
        System.out.println("Starting Manual Test Consumer...");

        try {
            // 1. Create Connection
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            Connection connection = factory.createConnection("admin", "admin");
            connection.start();

            // 2. Create Session
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // 3. Create Destination
            Queue destination = session.createQueue(QUEUE_NAME);

            // 4. Create Consumer
            MessageConsumer consumer = session.createConsumer(destination);

            System.out.println("Listening for messages on " + QUEUE_NAME + "...");

            // 5. Receive Message (with timeout of 10 seconds)
            Message message = consumer.receive(10000);

            if (message != null) {
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    String text = textMessage.getText();
                    System.out.println("Received TextMessage: " + text);

                    try {
                        IngestionEvent event = new Gson().fromJson(text, IngestionEvent.class);
                        System.out.println("Parsed Event - Book ID: " + event.bookId());
                    } catch (Exception e) {
                        System.err.println("Could not parse JSON: " + e.getMessage());
                    }

                } else {
                    System.out.println("Received non-text message: " + message);
                }
            } else {
                System.out.println("No message received within timeout.");
            }

            // 6. Cleanup
            session.close();
            connection.close();

        } catch (JMSException e) {
            System.err.println("JMS Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
