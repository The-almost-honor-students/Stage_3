package com.tahs.infrastructure.messaging;

import com.tahs.domain.IngestionEvent;
import org.apache.activemq.ActiveMQConnectionFactory;
import com.google.gson.Gson;

import javax.jms.*;

public class ManualTestConsumer {

    private static final String BROKER_URL = "failover:(tcp://192.168.1.133:61616,tcp://192.168.1.135:61616,tcp://192.168.1.143:61616)?randomize=false";
    private static final String QUEUE_NAME = "book.events";

    public static void main(String[] args) {
        System.out.println("Starting Manual Test Consumer...");

        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
            Connection connection = factory.createConnection("admin", "admin");
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue destination = session.createQueue(QUEUE_NAME);
            MessageConsumer consumer = session.createConsumer(destination);
            System.out.println("Listening for messages on " + QUEUE_NAME + "...");
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
            session.close();
            connection.close();

        } catch (JMSException e) {
            System.err.println("JMS Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
