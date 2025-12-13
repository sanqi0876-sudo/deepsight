package com.yidiansishiyi.deepsight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;

@SpringBootApplication
//@SpringBootApplication(exclude = {Neo4jAutoConfiguration.class})
public class DeepsightApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeepsightApplication.class, args);
    }

}
