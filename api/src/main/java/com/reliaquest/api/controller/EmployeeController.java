package com.reliaquest.api.controller;

import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class EmployeeController {

    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);
    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    // GET /api/v1/employees
    @GetMapping(value = "/employees", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Employee> getAllEmployees() {
        log.info("HIT getAllEmployees");
        return service.getAllEmployees();
    }

    // GET /api/v1/employees/search/{searchString}
    @GetMapping(value = "/employees/search/{searchString}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Employee> getEmployeesByNameSearch(@PathVariable String searchString) {
        log.info("HIT getEmployeesByNameSearch searchString={}", searchString);
        return service.getEmployeesByNameSearch(searchString);
    }

    // 👇 Add this
    @GetMapping(value = "/employees/highestSalary", produces = MediaType.APPLICATION_JSON_VALUE)
    public Integer getHighestSalaryOfEmployees() {
        log.info("HIT highestSalary");
        return service.getHighestSalaryOfEmployees();
    }


    // GET /api/v1/employees/topTenHighestEarningEmployeeNames
    @GetMapping(value = "/employees/topTenHighestEarningEmployeeNames", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getTop10HighestEarningEmployeeNames() {
        log.info("HIT getTop10HighestEarningEmployeeNames");
        return service.getTop10HighestEarningEmployeeNames();
    }

    // GET /api/v1/employees/{id}  (UUID-only so it won't match the static paths above)
    @GetMapping(value = "/employees/{id:[0-9a-fA-F\\-]{36}}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Employee getEmployeeById(@PathVariable String id) {
        log.info("HIT getEmployeeById id={}", id);
        return service.getEmployeeById(id);
    }

    // POST /api/v1/employees
    @PostMapping(
            value = "/employees",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Employee createEmployee(@Valid @RequestBody EmployeeCreateRequest input) {
        log.info(
                "HIT createEmployee name={}, salary={}, age={}, title={}",
                input.name(),
                input.salary(),
                input.age(),
                input.title());
        return service.createEmployee(input);
    }

    // DELETE /api/v1/employees/{id} (you can keep this ready)
    @DeleteMapping(value = "/employees/{id:[0-9a-fA-F\\-]{36}}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String deleteEmployeeById(@PathVariable String id) {
        log.info("HIT deleteEmployeeById id={}", id);
        return service.deleteEmployeeById(id); // returns the employee's NAME (plain text)
    }
}
