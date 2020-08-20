package com.example.demo;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
@Service
public class Consumer {

    public static final String CONSUMER_ID = "conciliadorRequestConsumer";

    private final Logger logger = LoggerFactory.getLogger(Consumer.class);
    private KafkaListenerEndpointRegistry registry;

    @Value("${ta.conciliador.request.thread.pool.size:1}")
    private int poolSize;

    int counter;
    private Semaphore semaphore;
    private ThreadPoolTaskExecutor executorPool;

    /**
     * Inits the.
     */
    @PostConstruct
    public void init() {

        this.logger.info("POOL SIZE ES DE {}", this.poolSize);
        this.semaphore = new Semaphore(this.poolSize);
    }

    /**
     * Sets the registry.
     *
     * @param registry the new registry
     */
    @Autowired
    public void setRegistry(final KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    /**
     * Setter.
     * @param pExecutorPool the executorPool to set
     */
    @Autowired
    @Qualifier("conciliadorRequestThreadPool")
    public void setExecutorPool(final ThreadPoolTaskExecutor pExecutorPool) {
        this.executorPool = pExecutorPool;
    }

    /**
     * Consume.
     *
     * @param message the message
     * @param ack the ack
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InterruptedException
     */
    @KafkaListener(id = CONSUMER_ID, topics = "${ta.conciliator.batch.kafka.queue}", groupId = "${ta.conciliator.batch.kafka.groupId}")
    public void consume(final String message, final Acknowledgment ack) throws IOException, InterruptedException {

        try {

            this.logger.info(String.format("#%d -> Consuming message -> %s", ++this.counter, message));

            this.semaphore.acquire();

            try {
                this.logger.info("Sending to thread-pool");
                this.executorPool.execute(() -> run(message, this.counter));
            }
            catch (RuntimeException ex) {
                // if execute fails we need to release semaphore.
                this.semaphore.release();
                throw ex;
            }

            if (this.semaphore.availablePermits() <= 0) {
                this.logger.info("Pausing Kafka poll");
                this.registry.getListenerContainer(CONSUMER_ID).pause();
            }

            this.logger.info("Message Consumer Ended sending request");
        }
        finally {
            ack.acknowledge();
        }
    }

    private Random random = new Random(System.currentTimeMillis());

    private void run(final String message, final int counter) {

        this.logger.info(String.format("Processing thread %s for message #(%d) %s"
                , Thread.currentThread().getName()
                , counter
                , message
                ));

        long millis = 8000L + this.random.nextInt(5000);

        try {
            this.logger.info("Sleeping {} millis...", millis);
            Thread.sleep(millis);
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }

        this.logger.info(String.format("###-001 -> Finished with message -> %s", message));

        this.semaphore.release();
        this.registry.getListenerContainer(CONSUMER_ID).resume();

    }




}