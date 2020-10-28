package com.example.demo;

import java.util.function.Consumer;

import org.apache.commons.lang3.Validate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * The Class KafkaSyncProducerHelper.
 *
 * @author tonioc
 */
public final class KafkaSyncProducerHelper {

    /**
     * Constructor.
     */
    private KafkaSyncProducerHelper() {
    }

    /**
     * Send message, and return true or false if message was delivered successful or not,
     * you'll need to provide an errorHandler to handle error cases.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param kafkaTemplate the kafka template
     * @param topic the topic
     * @param key the key may be null
     * @param message the message
     * @param errorHandler the error handler a Consumer<Throwable>
     * @return true, if successful
     */
    public static <K, V> boolean sendMessage(final KafkaTemplate<K, V> kafkaTemplate
            , final String topic
            , final K key
            , final V message
            , final Consumer<Throwable> errorHandler
            ) {

        Validate.notEmpty(topic, "'topic' must not be empty/null");
        Validate.notNull(message, "'message' must not be null");
        Validate.notNull(errorHandler, "'errorHandler' must not be null");

        ListenableFuture<SendResult<K, V>> future = kafkaTemplate.send(topic, key, message);

        try {
            kafkaTemplate.flush();
            future.get();
            return true;
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            errorHandler.accept(ex);
        }
        catch (Exception ex) {
            errorHandler.accept(ex);
        }

        return false;
    }

}
