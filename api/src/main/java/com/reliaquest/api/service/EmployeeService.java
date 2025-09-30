package com.reliaquest.api.service;

import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import java.util.List;

public interface EmployeeService {

    List<Employee> getAllEmployees();

    List<Employee> getEmployeesByNameSearch(String searchName);

    Employee getEmployeeById(String Id);

    int getHighestSalaryOfEmployees();

    List<String> getTop10HighestEarningEmployeeNames();

    Employee createEmployee(EmployeeCreateRequest req);

    String deleteEmployeeById(String empId);
}
