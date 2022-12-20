package com.rulebased848.gamsibot.core;

import com.rulebased848.gamsibot.domain.Request;
import com.rulebased848.gamsibot.domain.RequestRepository;
import com.rulebased848.gamsibot.domain.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RequestManager implements CommandLineRunner {
    private final RequestRepository repository;

    private final ThreadPool pool;

    private final TaskDefinition taskDef;

    @Autowired
    public RequestManager(RequestRepository repository, ThreadPool pool, TaskDefinition taskDef) {
        this.repository = repository;
        this.pool = pool;
        this.taskDef = taskDef;
    }

    @Override
    public void run(String... args) throws Exception {
        new TaskProviderThread().start();
    }

    public Request createRequest(Request request) {
        return repository.save(request);
    }

    public Map<String,Object> deleteRequest(long id, String username) {
        Map<String,Object> result = new HashMap<>();
        result.put("success", false);
        Optional<Request> maybeRequest = repository.findById(id);
        if (maybeRequest.isEmpty()) {
            result.put("reason", "The request ID is not valid.");
            return result;
        }
        Request request = maybeRequest.get();
        User user = request.getRequester();
        if (user != null && !user.getUsername().equals(username)) {
            result.put("reason", "You do not have permission.");
            return result;
        }
        repository.delete(request);
        result.put("success", true);
        return result;
    }

    private class TaskProviderThread extends Thread {
        @Override
        public void run() {
            while (true) {
                List<String> handles = repository.findAllDistinctHandle();
                for (String handle : handles) {
                    Runnable task = taskDef.getTask(handle);
                    pool.execute(task);
                }
                for (int i = 0; i < handles.size(); ++i) {
                    pool.receive();
                }
            }
        }
    }
}