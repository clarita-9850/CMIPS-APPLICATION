package com.cmips.controller;

import com.cmips.entity.Task;
import com.cmips.entity.WorkQueue;
import com.cmips.entity.WorkQueueSubscription;
import com.cmips.repository.WorkQueueRepository;
import com.cmips.service.KeycloakAdminService;
import com.cmips.service.KeycloakPolicyEvaluationService;
import com.cmips.service.TaskLifecycleService;
import com.cmips.service.TaskService;
import com.cmips.service.WorkQueueSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WorkQueueController
 *
 * Tests cover:
 * - Queue listing and catalog
 * - Subscription management
 * - Queue summary operations
 * - User listing
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("WorkQueueController Tests")
class WorkQueueControllerTest {

    @Mock
    private WorkQueueRepository workQueueRepository;

    @Mock
    private TaskService taskService;

    @Mock
    private TaskLifecycleService lifecycleService;

    @Mock
    private WorkQueueSubscriptionService subscriptionService;

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @Mock
    private KeycloakPolicyEvaluationService policyEvaluationService;

    @InjectMocks
    private WorkQueueController workQueueController;

    private static final String TEST_QUEUE = "address-changes";
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        // Setup can be added here if needed
    }

    @Test
    @DisplayName("Should get all active queues successfully")
    void testGetAllQueues_Success() {
        // Arrange
        List<WorkQueue> mockQueues = createMockWorkQueues();
        when(workQueueRepository.findByActiveTrue()).thenReturn(mockQueues);

        // Act
        ResponseEntity<List<WorkQueue>> response = workQueueController.getAllQueues();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(workQueueRepository, times(1)).findByActiveTrue();
    }

    @Test
    @DisplayName("Should get queue catalog (delegates to getAllQueues)")
    void testGetQueueCatalog_Success() {
        // Arrange
        List<WorkQueue> mockQueues = createMockWorkQueues();
        when(workQueueRepository.findByActiveTrue()).thenReturn(mockQueues);

        // Act
        ResponseEntity<List<WorkQueue>> response = workQueueController.getQueueCatalog();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("Should get queue by ID successfully")
    void testGetQueueById_Success() {
        // Arrange
        Long queueId = 1L;
        WorkQueue mockQueue = createMockWorkQueue(queueId, TEST_QUEUE);
        when(workQueueRepository.findById(queueId)).thenReturn(Optional.of(mockQueue));

        // Act
        ResponseEntity<WorkQueue> response = workQueueController.getQueueById(queueId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(TEST_QUEUE, response.getBody().getName());
    }

    @Test
    @DisplayName("Should return NotFound for non-existent queue")
    void testGetQueueById_NotFound() {
        // Arrange
        when(workQueueRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<WorkQueue> response = workQueueController.getQueueById(999L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Should get queue tasks successfully")
    void testGetQueueTasks_Success() {
        // Arrange
        Long queueId = 1L;
        WorkQueue mockQueue = createMockWorkQueue(queueId, TEST_QUEUE);
        List<Task> mockTasks = createMockTasks(3);
        when(workQueueRepository.findById(queueId)).thenReturn(Optional.of(mockQueue));
        when(taskService.getQueueTasks(TEST_QUEUE)).thenReturn(mockTasks);

        // Act
        ResponseEntity<?> response = workQueueController.getQueueTasks(queueId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should get all users successfully")
    void testGetAllUsers_Success() {
        // Arrange
        List<Map<String, Object>> mockUsers = createMockUsers();
        when(keycloakAdminService.getAllUsers()).thenReturn(mockUsers);

        // Act
        ResponseEntity<List<Map<String, Object>>> response = workQueueController.getAllUsers();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(keycloakAdminService, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("Should get queues summary successfully")
    void testGetQueuesSummary_Success() {
        // Arrange
        List<WorkQueue> mockQueues = createMockWorkQueues();
        when(workQueueRepository.findByActiveTrue()).thenReturn(mockQueues);
        when(taskService.getQueueTaskCountByStatus(anyString(), eq(Task.TaskStatus.OPEN))).thenReturn(2L);

        // Act
        ResponseEntity<List<Map<String, Object>>> response = workQueueController.getQueuesSummary();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("Should get queue subscribers successfully")
    void testGetQueueSubscribers_Success() {
        // Arrange
        Long queueId = 1L;
        WorkQueue mockQueue = createMockWorkQueue(queueId, TEST_QUEUE);
        List<WorkQueueSubscription> mockSubscriptions = createMockSubscriptions();
        when(workQueueRepository.findById(queueId)).thenReturn(Optional.of(mockQueue));
        when(subscriptionService.getQueueSubscriptions(TEST_QUEUE)).thenReturn(mockSubscriptions);

        // Act
        ResponseEntity<List<WorkQueueSubscription>> response = workQueueController.getQueueSubscribers(queueId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    // Helper methods
    private WorkQueue createMockWorkQueue(Long id, String name) {
        WorkQueue queue = new WorkQueue();
        queue.setId(id);
        queue.setName(name);
        queue.setDisplayName(name);
        queue.setActive(true);
        queue.setSupervisorOnly(false);
        return queue;
    }

    private List<WorkQueue> createMockWorkQueues() {
        return Arrays.asList(
                createMockWorkQueue(1L, "address-changes"),
                createMockWorkQueue(2L, "status-updates")
        );
    }

    private List<Task> createMockTasks(int count) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Task task = new Task();
            task.setId((long) i);
            task.setTitle("Test Task " + i);
            task.setWorkQueue(TEST_QUEUE);
            tasks.add(task);
        }
        return tasks;
    }

    private List<Map<String, Object>> createMockUsers() {
        return Arrays.asList(
                Map.of("id", "user1", "username", "user1", "email", "user1@example.com"),
                Map.of("id", "user2", "username", "user2", "email", "user2@example.com")
        );
    }

    private List<WorkQueueSubscription> createMockSubscriptions() {
        List<WorkQueueSubscription> subscriptions = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            WorkQueueSubscription sub = new WorkQueueSubscription();
            sub.setId((long) i);
            sub.setUsername("user" + i);
            sub.setWorkQueue(TEST_QUEUE);
            subscriptions.add(sub);
        }
        return subscriptions;
    }
}
