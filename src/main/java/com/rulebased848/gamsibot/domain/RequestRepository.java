package com.rulebased848.gamsibot.domain;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface RequestRepository extends CrudRepository<Request,Long> {
    void deleteByHandle(String handle);

    List<Request> findByHandleAndTargetSubscriberCountLessThanEqual(String handle, long targetSubscriberCount);

    @Query("SELECT MIN(r.targetSubscriberCount) FROM Request r WHERE r.handle = ?1")
    Long findMinimumTargetSubscriberCountByHandle(String handle);

    @Query("SELECT new com.rulebased848.gamsibot.domain.HandleAndTargetSubscriberCount(r.handle, MIN(r.targetSubscriberCount)) FROM Request r GROUP BY r.handle")
    List<HandleAndTargetSubscriberCount> findAllHandleWithMinimumTargetSubscriberCount();
}