package com.tahs.infrastructure.messaging;

import com.google.gson.Gson;
import com.tahs.domain.IngestionEvent;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.time.LocalDateTime;

public class ManualTestProducer {

    private static final String BROKER_URL = "failover:(tcp://192.168.1.133:61616,tcp://192.168.1.135:61616,tcp://192.168.1.143:61616)?randomize=false";
    private static final String QUEUE_NAME = "book.events";

    public static void main(String[] args) {
        System.out.println("Starting Manual Test Producer...");

        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            Connection connection = factory.createConnection("admin", "admin");
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue destination = session.createQueue(QUEUE_NAME);
            MessageProducer producer = session.createProducer(destination);
            String bookId = "999999";
            String timestamp = LocalDateTime.now().toString();
            String eventType = "BOOK_DOWNLOADED";
            String filePath = "test/path/to/book_999999.txt";

            IngestionEvent event = new IngestionEvent(bookId, timestamp, eventType, filePath);
            String jsonPayload = new Gson().toJson(event);
            TextMessage message = session.createTextMessage(jsonPayload);
            producer.send(message);

            System.out.println("Sent Test Message: " + jsonPayload);
            System.out.println("Check the Indexer logs. It should attempt to index book " + bookId);
            session.close();
            connection.close();

        } catch (JMSException e) {
            System.err.println("JMS Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
