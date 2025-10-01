package com.reliaquest.api.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import java.util.UUID;

import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(controllers = EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private static Employee emp(String id, String name, int salary, int age, String title, String email) {
        return new Employee(id, name, salary, age, title, email);
    }

    // ----------------- GET /employees -----------------
    @Test
    @DisplayName("GET /api/v1/employees -> 200 + list")
    void getAllEmployees_ok() throws Exception {
        // ARRANGE
        var list = List.of(
                emp(UUID.randomUUID().toString(), "Alice", 120_000, 31, "Engineer", "alice@x.com"),
                emp(UUID.randomUUID().toString(), "Bob", 90_000, 28, "QA", "bob@x.com"));
        Mockito.when(employeeService.getAllEmployees()).thenReturn(list);

        // ACT / ASSERT
        mvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].employee_name", is("Alice")))
                .andExpect(jsonPath("$[1].employee_salary", is(90000)));

        // ASSERT
        Mockito.verify(employeeService, Mockito.times(1)).getAllEmployees();
    }

    // ------------- GET /employees/search/{fragment} -------------
    @Test
    @DisplayName("GET /api/v1/employees/search/{q} -> 200 + filtered list")
    void search_ok() throws Exception {
        // ARRANGE
        var list = List.of(emp(UUID.randomUUID().toString(), "Rosario O'Kon", 130_000, 40, "Mgr", "r@x.com"));
        Mockito.when(employeeService.getEmployeesByNameSearch(eq("rosa"))).thenReturn(list);

        // ACT / ASSERT
        mvc.perform(get("/api/v1/employees/search/{q}", "rosa"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].employee_name", containsString("Rosario")));

        // ASSERT
        Mockito.verify(employeeService, Mockito.times(1)).getEmployeesByNameSearch(eq("rosa"));
    }

    // ------------- GET /employees/highestSalary -------------
    @Test
    @DisplayName("GET /api/v1/employees/highestSalary -> 200 + integer")
    void highestSalary_ok() throws Exception {
        // ARRANGE
        Mockito.when(employeeService.getHighestSalaryOfEmployees()).thenReturn(320_800);

        // ACT / ASSERT
        mvc.perform(get("/api/v1/employees/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("320800"));

        // Additional Assertions
        Mockito.verify(employeeService, Mockito.times(1)).getHighestSalaryOfEmployees();
    }

    // ------------- GET /employees/topTenHighestEarningEmployeeNames -------------
    @Test
    @DisplayName("GET /api/v1/employees/topTenHighestEarningEmployeeNames -> 200 + list of names")
    void topTen_ok() throws Exception {
        // ARRANGE
        var names = List.of("Alice", "Bob", "Carol", "Dan", "Eve", "Frank", "Grace", "Heidi", "Ivan", "Judy");
        Mockito.when(employeeService.getTop10HighestEarningEmployeeNames()).thenReturn(names);

        // ACT / ASSERT
        mvc.perform(get("/api/v1/employees/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0]", is("Alice")));

        // Additional Assertions
        Mockito.verify(employeeService, Mockito.times(1)).getTop10HighestEarningEmployeeNames();
    }

    // ----------------- GET /employees/{id} -----------------
    @Test
    @DisplayName("GET /api/v1/employees/{id} -> 200 + employee")
    void getById_ok() throws Exception {
        // ARRANGE
        String id = UUID.randomUUID().toString();
        var e = emp(id, "Jane Doe", 123_000, 33, "SWE", "jane@x.com");
        Mockito.when(employeeService.getEmployeeById(eq(id))).thenReturn(e);

        // ACT / ASSERT
        mvc.perform(get("/api/v1/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.employee_name", is("Jane Doe")))
                .andExpect(jsonPath("$.employee_salary", is(123000)));

        // Additional Assertions
        Mockito.verify(employeeService, Mockito.times(1)).getEmployeeById(eq(id));

    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} -> 404 when service throws NOT_FOUND")
    void getById_notFound() throws Exception {
        // ARRANGE
        String id = UUID.randomUUID().toString();
        Mockito.when(employeeService.getEmployeeById(eq(id))).thenThrow(new ResponseStatusException(NOT_FOUND, "not found"));

        // ACT
        mvc.perform(get("/api/v1/employees/{id}", id))
                .andExpect(status().isNotFound());

        // ASSERT
        Mockito.verify(employeeService, Mockito.times(1)).getEmployeeById(eq(id));
    }

    // ----------------- POST  /employees -----------------
    @Test
    void createEmployee_success() throws Exception {
        // ARRANGE
        var request = new EmployeeCreateRequest("Jon Doe", 110500, 26, "Software Engineer");

        // ACT
        mvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // ASSERT
        ArgumentCaptor<EmployeeCreateRequest> captor = ArgumentCaptor.forClass(EmployeeCreateRequest.class);
        Mockito.verify(employeeService, Mockito.times(1)).createEmployee(captor.capture());
        EmployeeCreateRequest captured = captor.getValue();
        assertEquals("Jon Doe", captured.name());
        assertEquals(110500, captured.salary());
        assertEquals(26, captured.age());
        assertEquals("Software Engineer", captured.title());
    }



    // ----------------- DELETE /employees/{id} -----------------
    @Test
    @DisplayName("DELETE /api/v1/employees/{id} -> 200 + name (text/plain)")
    void delete_ok() throws Exception {
        // ARRANGE
        String id = UUID.randomUUID().toString();
        Mockito.when(employeeService.deleteEmployeeById(eq(id))).thenReturn("Jane Doe");

        // ACT
        mvc.perform(delete("/api/v1/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Jane Doe")); // String that is Returned when delete is successful

        // ASSERT
        Mockito.verify(employeeService, Mockito.times(1)).deleteEmployeeById(eq(id));

    }

    @Test
    @DisplayName("DELETE /api/v1/employees/{id} -> 404 when service says not found")
    void delete_notFound() throws Exception {
        // ARRANGE
        String id = UUID.randomUUID().toString();
        Mockito.when(employeeService.deleteEmployeeById(eq(id))).thenThrow(new ResponseStatusException(NOT_FOUND, "not found"));

        // ACT
        mvc.perform(delete("/api/v1/employees/{id}", id)).andExpect(status().isNotFound());

        // ASSERT
        Mockito.verify(employeeService, Mockito.times(1)).deleteEmployeeById(eq(id));
    }
}
