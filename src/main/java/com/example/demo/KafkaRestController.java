package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DOCUMENT .
 * @author tonioc
 *
 */
@RestController
@RequestMapping(value = "/kafka")
public class KafkaRestController {

    private final Producer producer;

    @Autowired
    KafkaRestController(final Producer producer) {
        this.producer = producer;
    }

    @PostMapping(value = "/publish")
    public void sendMessageToKafkaTopic(@RequestBody() final String message) {
        this.producer.sendMessage(message);
    }
}