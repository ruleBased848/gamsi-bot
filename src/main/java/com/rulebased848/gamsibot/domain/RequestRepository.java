package com.rulebased848.gamsibot.domain;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface RequestRepository extends CrudRepository<Request,Long> {
    void deleteByHandle(String handle);

    List<Request> findByHandleAndTargetSubscriberCountLessThanEqual(String handle, long targetSubscriberCount);

    @Query("SELECT DISTINCT(r.handle) FROM Request r")
    List<String> findAllDistinctHandle();
}