package com.siddesh.expensetracker.mongo.service;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.siddesh.expensetracker.mongo.document.DatabaseSequence;

@Service
public class SequenceGeneratorService {

    private final MongoOperations mongoOperations;

    public SequenceGeneratorService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    public long getNextSequence(String sequenceName) {
        Query query = Query.query(Criteria.where("_id").is(sequenceName));
        Update update = new Update().inc("value", 1);
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(true);

        DatabaseSequence counter = mongoOperations.findAndModify(query, update, options, DatabaseSequence.class);
        return counter != null ? counter.getValue() : 1;
    }
}
