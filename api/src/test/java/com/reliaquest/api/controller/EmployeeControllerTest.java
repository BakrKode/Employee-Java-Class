package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService service;

    private static Employee emp(String id, String name, int salary, int age, String title, String email) {
        return new Employee(id, name, salary, age, title, email);
    }

    // ----------------- GET /employees -----------------
    @Test
    @DisplayName("GET /api/v1/employees -> 200 + list")
    void getAllEmployees_ok() throws Exception {
        var list = List.of(
                emp(UUID.randomUUID().toString(), "Alice", 120_000, 31, "Engineer", "alice@x.com"),
                emp(UUID.randomUUID().toString(), "Bob", 90_000, 28, "QA", "bob@x.com")
        );
        Mockito.when(service.getAllEmployees()).thenReturn(list);

        mvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].employee_name", is("Alice")))
                .andExpect(jsonPath("$[1].employee_salary", is(90000)));
    }

    // ------------- GET /employees/search/{fragment} -------------
    @Test
    @DisplayName("GET /api/v1/employees/search/{q} -> 200 + filtered list")
    void search_ok() throws Exception {
        var list = List.of(
                emp(UUID.randomUUID().toString(), "Rosario O'Kon", 130_000, 40, "Mgr", "r@x.com")
        );
        Mockito.when(service.getEmployeesByNameSearch(eq("rosa"))).thenReturn(list);

        mvc.perform(get("/api/v1/employees/search/{q}", "rosa"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].employee_name", containsString("Rosario")));
    }

    // ------------- GET /employees/highestSalary -------------
    @Test
    @DisplayName("GET /api/v1/employees/highestSalary -> 200 + integer")
    void highestSalary_ok() throws Exception {
        Mockito.when(service.getHighestSalaryOfEmployees()).thenReturn(320_800);

        mvc.perform(get("/api/v1/employees/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("320800"));
    }

    // ------------- GET /employees/topTenHighestEarningEmployeeNames -------------
    @Test
    @DisplayName("GET /api/v1/employees/topTenHighestEarningEmployeeNames -> 200 + list of names")
    void topTen_ok() throws Exception {
        var names = List.of("Alice", "Bob", "Carol", "Dan", "Eve", "Frank", "Grace", "Heidi", "Ivan", "Judy");
        Mockito.when(service.getTop10HighestEarningEmployeeNames()).thenReturn(names);

        mvc.perform(get("/api/v1/employees/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0]", is("Alice")));
    }

    // ----------------- GET /employees/{id} -----------------
    @Test
    @DisplayName("GET /api/v1/employees/{id} -> 200 + employee")
    void getById_ok() throws Exception {
        String id = UUID.randomUUID().toString();
        var e = emp(id, "Jane Doe", 123_000, 33, "SWE", "jane@x.com");
        Mockito.when(service.getEmployeeById(eq(id))).thenReturn(e);

        mvc.perform(get("/api/v1/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.employee_name", is("Jane Doe")))
                .andExpect(jsonPath("$.employee_salary", is(123000)));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} -> 404 when service throws NOT_FOUND")
    void getById_notFound() throws Exception {
        String id = UUID.randomUUID().toString();
        Mockito.when(service.getEmployeeById(eq(id)))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "not found"));

        mvc.perform(get("/api/v1/employees/{id}", id))
                .andExpect(status().isNotFound());
    }


    // ----------------- DELETE /employees/{id} -----------------
    @Test
    @DisplayName("DELETE /api/v1/employees/{id} -> 200 + name (text/plain)")
    void delete_ok() throws Exception {
        String id = UUID.randomUUID().toString();
        Mockito.when(service.deleteEmployeeById(eq(id))).thenReturn("Jane Doe");

        mvc.perform(delete("/api/v1/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Jane Doe"));
    }

    @Test
    @DisplayName("DELETE /api/v1/employees/{id} -> 404 when service says not found")
    void delete_notFound() throws Exception {
        String id = UUID.randomUUID().toString();
        Mockito.when(service.deleteEmployeeById(eq(id)))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "not found"));

        mvc.perform(delete("/api/v1/employees/{id}", id))
                .andExpect(status().isNotFound());
    }
}
