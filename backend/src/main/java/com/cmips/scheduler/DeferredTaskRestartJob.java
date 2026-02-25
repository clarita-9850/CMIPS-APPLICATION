package com.cmips.scheduler;

import com.cmips.entity.Task;
import com.cmips.entity.TaskHistory;
import com.cmips.repository.TaskRepository;
import com.cmips.repository.TaskHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job that restarts deferred tasks when their restart date arrives.
 * Runs every hour.
 */
@Component
public class DeferredTaskRestartJob {

    private static final Logger log = LoggerFactory.getLogger(DeferredTaskRestartJob.class);

    private final TaskRepository taskRepository;
    private final TaskHistoryRepository historyRepository;

    public DeferredTaskRestartJob(TaskRepository taskRepository,
                                  TaskHistoryRepository historyRepository) {
        this.taskRepository = taskRepository;
        this.historyRepository = historyRepository;
    }

    @Scheduled(fixedDelay = 3600000) // Every hour
    @Transactional
    public void restartDeferredTasks() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Deferred task restart check at {}", now);

        List<Task> readyTasks = taskRepository.findDeferredTasksReadyToRestart(now);

        int restarted = 0;
        for (Task task : readyTasks) {
            try {
                String previousStatus = task.getStatus().name();
                task.setStatus(Task.TaskStatus.OPEN);
                task.setDeferredBy(null);
                task.setDeferredDate(null);
                task.setRestartDate(null);
                task.setUpdatedAt(LocalDateTime.now());
                taskRepository.save(task);

                TaskHistory history = new TaskHistory();
                history.setTaskId(task.getId());
                history.setAction(TaskHistory.TaskAction.RESTARTED);
                history.setPerformedBy("SYSTEM");
                history.setPerformedAt(LocalDateTime.now());
                history.setPreviousStatus(previousStatus);
                history.setNewStatus("OPEN");
                history.setDetails("Auto-restarted from deferred state — restart date reached");
                historyRepository.save(history);

                restarted++;
            } catch (Exception e) {
                log.error("Error restarting deferred task {}: {}", task.getId(), e.getMessage());
            }
        }

        log.info("Deferred restart complete: {} tasks restarted", restarted);
    }
}
