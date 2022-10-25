package com.rulebased848.gamsibot.domain;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface RequestRepository extends CrudRepository<Request,Long> {
    void deleteByChannelId(String channelId);

    List<Request> findByChannelIdAndTargetSubscriberCountLessThanEqual(String channelId, long targetSubscriberCount);

    @Query("SELECT MIN(r.targetSubscriberCount) FROM Request r WHERE r.channelId = ?1")
    Long findMinimumTargetSubscriberCountByChannelId(String channelId);

    @Query("SELECT new com.rulebased848.gamsibot.domain.ChannelIdAndTargetSubscriberCount(r.channelId, MIN(r.targetSubscriberCount)) FROM Request r GROUP BY r.channelId")
    List<ChannelIdAndTargetSubscriberCount> findAllChannelIdWithMinimumTargetSubscriberCount();
}