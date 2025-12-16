package com.yidiansishiyi.deepsight;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.repository.config.EnableReactiveNeo4jRepositories;

@SpringBootApplication
@EnableNeo4jRepositories(basePackages = "com.yidiansishiyi.deepsight.graph.repository")
@MapperScan("com.yidiansishiyi.deepsight.mapper")
//@SpringBootApplication(exclude = {Neo4jAutoConfiguration.class})
public class DeepsightApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeepsightApplication.class, args);
    }

}
