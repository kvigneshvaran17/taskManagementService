package org.example.task.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.TaskManagementApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TaskManagementApplication.class)
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void create_get_update_delete_flow() throws Exception {
        String createBody = objectMapper.writeValueAsString(Map.of(
                "title", "My Task",
                "description", "Desc",
                "status", "PENDING",
                "due_date", LocalDate.now().plusDays(2).toString()
        ));

        String createdJson = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.title").value("My Task"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(createdJson).get("id").asText();

        mockMvc.perform(get("/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("My Task"));

        String updateBody = objectMapper.writeValueAsString(Map.of(
                "status", "DONE"
        ));

        mockMvc.perform(put("/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));

        mockMvc.perform(delete("/tasks/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Task not found")));
    }

    @Test
    void create_requires_title_and_due_date() throws Exception {
        String bodyMissingTitle = objectMapper.writeValueAsString(Map.of(
                "due_date", LocalDate.now().plusDays(2).toString()
        ));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyMissingTitle))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        String bodyMissingDueDate = objectMapper.writeValueAsString(Map.of(
                "title", "x"
        ));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyMissingDueDate))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void list_is_sorted_and_can_filter_and_paginate() throws Exception {
        String baseDue = LocalDate.now().plusDays(10).toString();

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "title", "t1",
                        "status", "PENDING",
                        "due_date", LocalDate.now().plusDays(5).toString()
                )))).andExpect(status().isCreated());

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "title", "t2",
                        "status", "DONE",
                        "due_date", LocalDate.now().plusDays(2).toString()
                )))).andExpect(status().isCreated());

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "title", "t3",
                        "status", "PENDING",
                        "due_date", baseDue
                )))).andExpect(status().isCreated());

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$[0].due_date", is(LocalDate.now().plusDays(2).toString())));

        mockMvc.perform(get("/tasks").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status", everyItem(is("PENDING"))));

        mockMvc.perform(get("/tasks").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void delete_nonexistent_returns_404() throws Exception {
        mockMvc.perform(delete("/tasks/{id}", "does-not-exist"))
                .andExpect(status().isNotFound());
    }
}
