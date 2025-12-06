package com.tahs.infrastructure.messaging;

import com.google.gson.Gson;
import com.tahs.application.ports.EventPublisher;
import com.tahs.domain.BookEvent;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ActiveMQEventPublisher implements EventPublisher {

    private final String brokerUrl;
    private final String topicName;
    private final Gson gson;
    private Connection connection;
    private Session session;
    private MessageProducer producer;

    public ActiveMQEventPublisher(String brokerUrl, String topicName) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.gson = new Gson();
        initialize();
    }

    private void initialize() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            factory.setUserName("admin");
            factory.setPassword("admin");
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue destination = session.createQueue(topicName);
            producer = session.createProducer(destination);
        } catch (JMSException e) {
            throw new RuntimeException("Failed to initialize ActiveMQ publisher", e);
        }
    }

    @Override
    public void publish(BookEvent event) {
        try {
            String json = gson.toJson(event);
            TextMessage message = session.createTextMessage(json);
            producer.send(message);
            System.out.println("[ActiveMQ] Published event for book: " + event.bookId());
        } catch (JMSException e) {
            System.err.println("[ActiveMQ] Error publishing event: " + e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        if (producer != null)
            producer.close();
        if (session != null)
            session.close();
        if (connection != null)
            connection.close();
    }
}
