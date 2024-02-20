package com.firisbe.aspect.sequence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import org.springframework.stereotype.Component;

@Component
public class SequenceGenerator {
    @Autowired
    private MongoOperations mongoOperations;

    public Long generateSequence(Class<?> clazz) {
        SequenceCounter counter = mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is(clazz.getSimpleName())),
                new Update().inc("seq", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                SequenceCounter.class);

        assert counter != null;
        return counter.getSeq();
    }
}
