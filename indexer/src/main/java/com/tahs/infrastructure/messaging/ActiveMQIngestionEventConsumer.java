package com.tahs.infrastructure.messaging;

import com.google.gson.Gson;
import com.tahs.application.ports.EventConsumer;
import com.tahs.application.usecase.IndexService;
import com.tahs.domain.IngestionEvent;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ActiveMQIngestionEventConsumer implements EventConsumer {

    private final String brokerUrl;
    private final String queueName;
    private final IndexService indexService;
    private final Gson gson;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;
    private final String username;
    private final String password;

    public ActiveMQIngestionEventConsumer(String brokerUrl,
            String queueName,
            String username,
            String password,
            IndexService indexService) {
        this.brokerUrl = brokerUrl;
        this.queueName = queueName;
        this.indexService = indexService;
        this.gson = new Gson();
        this.username = username;
        this.password = password;
    }

    @Override
    public void startListening() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            if (username != null && password != null) {
                factory.setUserName(username);
                factory.setPassword(password);
                connection = factory.createConnection(username, password);
            } else {
                connection = factory.createConnection();
            }

            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue destination = session.createQueue(queueName);
            consumer = session.createConsumer(destination);

            consumer.setMessageListener(message -> {
                if (message instanceof TextMessage) {
                    try {
                        String json = ((TextMessage) message).getText();
                        IngestionEvent event = gson.fromJson(json, IngestionEvent.class);
                        if ("BOOK_DOWNLOADED".equals(event.eventType())) {
                            System.out.println("[ActiveMQ] Received download event for book: " + event.bookId());
                            indexService.updateByBookId(event.bookId());
                        }
                    } catch (Exception e) {
                        System.err.println("[ActiveMQ] Error processing message: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

            System.out.println("[ActiveMQ] Listening on queue: " + queueName);

        } catch (JMSException e) {
            throw new RuntimeException("Failed to start ActiveMQ consumer", e);
        }
    }

    @Override
    public void close() throws Exception {
        if (consumer != null)
            consumer.close();
        if (session != null)
            session.close();
        if (connection != null)
            connection.close();
    }
}
