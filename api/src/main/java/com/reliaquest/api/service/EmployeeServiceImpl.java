package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);
    private final EmployeeClient employeeClient;

    public EmployeeServiceImpl(EmployeeClient employeeClient) {
        this.employeeClient = employeeClient;
    }

    @Override
    public List<Employee> getAllEmployees() {
        var resp = employeeClient.getAll(); // ApiListResponse<Employee>
        var list = (resp == null || resp.data() == null) ? List.<Employee>of() : resp.data();
        log.info("Service.getAllEmployees -> size={}", list.size());
        if (!list.isEmpty()) {
            var first = list.get(0);
            log.debug("Employee: id={}, name={}", first.id(), first.employeeName());
        }
        return list;
    }

    @Override
    public List<Employee> getEmployeesByNameSearch(String emplName) {
        log.debug("Service: getEmployeesByNameSearch({})", emplName);
        String f = (emplName == null ? "" : emplName).toLowerCase(Locale.ROOT);

        List<Employee> all = getAllEmployees();
        log.debug("Search base size={}", all.size());

        if (f.isEmpty()) {
            return all;
        }

        return all.stream()
                .filter(e -> {
                    String name = e.employeeName();
                    return name != null && name.toLowerCase(Locale.ROOT).contains(f);
                })
                .toList();
    }

    @Override
    public Employee getEmployeeById(String id) {
        log.debug("service: getEmployeeById({})", id);
        try {
            var resp = employeeClient.getById(id);
            if (resp == null || resp.data() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + id);
            }
            return resp.data();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + id, e);
        }
    }

    @Override
    public int getHighestSalaryOfEmployees() {
        var all = getAllEmployees();
        log.debug("Highest salary base size={}", all.size());

        int max = all.stream()
                .mapToInt(Employee::employeeSalary)
                .max()
                .orElse(0);

        log.info("Highest salary = {}", max);
        return max;
    }


    @Override
    public List<String> getTop10HighestEarningEmployeeNames() {
        log.debug("Service: getTop10HighestEarningEmployeeNames()");
        return getAllEmployees().stream()
                .sorted(Comparator.comparingInt(Employee::employeeSalary).reversed())
                .limit(10)
                .map(Employee::employeeName)
                .collect(Collectors.toList());
    }

    @Override
    public Employee createEmployee(EmployeeCreateRequest req) {
        log.debug(
                "Service: createEmployee(name={}, salary={}, age={}, title={})",
                req.name(),
                req.salary(),
                req.age(),
                req.title());
        var resp = employeeClient.create(req);
        if (resp == null || resp.data() == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Create failed");
        }
        return resp.data();
    }

    // in EmployeeServiceImpl
    @Override
    public String deleteEmployeeById(String id) {
        log.info("Service: delete by id={}", id);

        // fetch by id (so we get the current name in THIS dataset run)
        var resp = employeeClient.getById(id); // may throw WebClientResponseException.NotFound
        var emp = resp == null ? null : resp.data();
        if (emp == null || emp.employeeName() == null || emp.employeeName().isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Employee not found: " + id);
        }
        String name = emp.employeeName();

        // delete by name
        boolean ok = employeeClient.deleteByName(name);
        if (!ok) {
            // treat as not-found rather than 500
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    "Employee not found when deleting by name: " + name);
        }

        log.info("Service: deleted '{}'(id={})", name, id);
        return name;
    }

}
