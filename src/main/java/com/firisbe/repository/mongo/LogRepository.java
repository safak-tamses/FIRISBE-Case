package com.firisbe.repository.mongo;

import com.firisbe.model.Log;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository("mongoLogRepository")
public interface LogRepository extends MongoRepository<Log, String> {

}
