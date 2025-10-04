package com.siddesh.expensetracker.mongo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = {
	"com.siddesh.expensetracker.mongo.repository",
	"com.siddesh.expensetracker.repository"
})
public class MongoConfiguration {
}
