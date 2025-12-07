package com.tahs.infrastructure.messaging;

import com.tahs.application.ports.MessageBroker;
import com.tahs.config.ActiveMQClusterConfig;
import com.tahs.domain.BookEvent;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.Serializable;

public class ActiveMQBroker implements MessageBroker {
    private final String brokerUrl;
    private final String queueName;
    private Connection connection;
    private Session session;
    private MessageProducer producer;

    public ActiveMQBroker(String queueName) throws JMSException {
        this.brokerUrl = ActiveMQClusterConfig.getFailoverUrl();
        this.queueName = queueName;
        initialize();
    }

    public ActiveMQBroker(String brokerUrl, String queueName) throws JMSException {
        this.brokerUrl = brokerUrl;
        this.queueName = queueName;
        initialize();
    }

    private void initialize() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(queueName);
        producer = session.createProducer(destination);
    }

    @Override
    public void publish(BookEvent event) {
        try {
            ObjectMessage message = session.createObjectMessage((Serializable) event);
            producer.send(message);
        } catch (JMSException e) {
            throw new RuntimeException("Failed to publish event", e);
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
