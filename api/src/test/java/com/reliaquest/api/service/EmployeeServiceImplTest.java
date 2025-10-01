package com.reliaquest.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.dto.ApiListResponse;
import com.reliaquest.api.dto.ApiSingleResponse;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeClient employeeClient;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    // ------------ helpers ------------

    private static Employee emp(String name, int salary) {
        return new Employee(
                UUID.randomUUID().toString(), // Random User Id
                name,
                salary,
                30,
                "Engineer",
                name.toLowerCase().replace(" ", "") + "@example.com");
    }

    private static ApiListResponse<Employee> listResp(List<Employee> data) {
        return new ApiListResponse<>(data, "ok");
    }

    private static ApiSingleResponse<Employee> oneResp(Employee e) {
        return new ApiSingleResponse<>(e, "ok");
    }

    // ------------ getAllEmployees ------------

    @Test
    @DisplayName("getAllEmployees returns list from client")
    void getAllEmployees_ok() {
        // ARRANGE
        var e1 = emp("Alice", 120_000);
        var e2 = emp("Bob", 90_000);
        given(employeeClient.getAll())
                .willReturn(listResp(List.of(e1, e2)));

        // ACT
        var result = employeeService.getAllEmployees();

        // ASSERT
        assertThat(result).hasSize(2).containsExactly(e1, e2);
    }

    @Test
    @DisplayName("getAllEmployees returns empty when client/data is null")
    void getAllEmployees_emptyWhenNull() {
        // ARRANGE
        given(employeeClient.getAll()).willReturn(null);

        // ACT
        var result = employeeService.getAllEmployees();

        // ASSERT
        assertThat(result).isEmpty();
    }

    // ------------ getEmployeesByNameSearch ------------

    @Test
    @DisplayName("search filters by lowercase contains")
    void search_filters() {
        // ARRANGE
        var a = emp("Rosario O'Kon", 130_000);
        var b = emp("Jane Doe", 110_000);
        given(employeeClient.getAll()).willReturn(listResp(List.of(a, b)));

        // ACT
        var result = employeeService.getEmployeesByNameSearch("rosa");

        // ASSERT
        assertThat(result).containsExactly(a);
    }

    @Test
    @DisplayName("search with blank returns all")
    void search_blankReturnsAll() {
        // ARRANGE
        var a = emp("Alice", 1);
        var b = emp("Bob", 2);
        given(employeeClient.getAll()).willReturn(listResp(List.of(a, b)));

        // ASSERT
        assertThat(employeeService.getEmployeesByNameSearch(null)).containsExactly(a, b);
        assertThat(employeeService.getEmployeesByNameSearch("")).containsExactly(a, b);
    }

    // ------------ getEmployeeById ------------

    @Test
    @DisplayName("getById returns employee when client returns data")
    void getById_ok() {
        // ASSERT
        var e = emp("Jane Doe", 123_000);
        given(employeeClient.getById(e.id())).willReturn(oneResp(e));

        // ACT
        var out = employeeService.getEmployeeById(e.id());

        // ASSERT
        assertThat(out).isEqualTo(e);
    }

    @Test
    @DisplayName("getById throws 404 when client returns null data")
    void getById_notFound_nullData() {
        // ARRANGE
        var id = UUID.randomUUID().toString();
        given(employeeClient.getById(id)).willReturn(new ApiSingleResponse<>(null, "ok"));

        // ACT / ASSERT
        assertThatThrownBy(() -> employeeService.getEmployeeById(id))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ------------ getHighestSalaryOfEmployees ------------

    @Test
    @DisplayName("highestSalary returns max salary")
    void highestSalary_ok() {
        // ARRANGE
        given(employeeClient.getAll()).willReturn(listResp(List.of(emp("A", 50), emp("B", 70), emp("C", 60))));

        // ACT
        int max = employeeService.getHighestSalaryOfEmployees();

        // ASSERT
        assertThat(max).isEqualTo(70);
    }

    @Test
    @DisplayName("highestSalary returns 0 when empty")
    void highestSalary_empty() {
        // ARRANGE
        given(employeeClient.getAll()).willReturn(listResp(List.of()));

        // ACT / ASSERT
        assertThat(employeeService.getHighestSalaryOfEmployees()).isZero();
    }

    // ------------ getTop10HighestEarningEmployeeNames ------------

    @Test
    @DisplayName("top10 returns names of top 10 by salary, desc")
    void top10_ok() {
        // ARRANGE
        var list = List.of(
                emp("N1", 10),
                emp("N2", 20),
                emp("N3", 30),
                emp("N4", 40),
                emp("N5", 50),
                emp("N6", 60),
                emp("N7", 70),
                emp("N8", 80),
                emp("N9", 90),
                emp("N10", 100),
                emp("N11", 5),
                emp("N12", 1));
        given(employeeClient.getAll()).willReturn(listResp(list));

        // ACT
        var names = employeeService.getTop10HighestEarningEmployeeNames();

        // ASSERT
        assertThat(names).hasSize(10);
        assertThat(names.get(0)).isEqualTo("N10");
        assertThat(names.get(1)).isEqualTo("N9");
        assertThat(names.get(2)).isEqualTo("N8");
        assertThat(names.get(3)).isEqualTo("N7");
        assertThat(names.get(4)).isEqualTo("N6");
        assertThat(names.get(5)).isEqualTo("N5");
        assertThat(names.get(6)).isEqualTo("N4");
        assertThat(names.get(7)).isEqualTo("N3");
        assertThat(names.get(8)).isEqualTo("N2");
        assertThat(names.get(9)).isEqualTo("N1");
    }

    // ------------ createEmployee ------------

    @Test
    @DisplayName("create returns created employee")
    void create_ok() {
        // ARRANGE
        var req = new EmployeeCreateRequest("Jane Doe", 120_000, 33, "Senior SWE");
        var created = emp("Jane Doe", 120_000);
        given(employeeClient.create(req)).willReturn(oneResp(created));

        // ACT
        var out = employeeService.createEmployee(req);

        // ASSERT
        assertThat(out).isEqualTo(created);
    }

    @Test
    @DisplayName("create throws 500 when client returns null/empty")
    void create_failNull() {
        // ARRANGE
        var req = new EmployeeCreateRequest("Jane Doe", 120_000, 33, "Senior SWE");
        given(employeeClient.create(req)).willReturn(null);

        // ACT / ASSERT
        assertThatThrownBy(() -> employeeService.createEmployee(req))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ------------ deleteEmployeeById ------------

    @Test
    @DisplayName("delete by id returns deleted NAME when downstream succeeds")
    void delete_ok() {
        // ARRANGE
        var e = emp("Jane Doe", 100);
        given(employeeClient.getById(e.id())).willReturn(oneResp(e));
        given(employeeClient.deleteByName("Jane Doe")).willReturn(true);

        // ACT
        var out = employeeService.deleteEmployeeById(e.id());

        // ASSERT
        assertThat(out).isEqualTo("Jane Doe");
        then(employeeClient).should().deleteByName("Jane Doe");
    }

    @Test
    @DisplayName("delete by id -> 404 when deleteByName returns false")
    void delete_deleteByNameFalse() {
        // ARRANGE
        var e = emp("Ghost Name", 10);
        given(employeeClient.getById(e.id())).willReturn(oneResp(e));
        given(employeeClient.deleteByName("Ghost Name")).willReturn(false);

        // ACT / ASSERT
        assertThatThrownBy(() -> employeeService.deleteEmployeeById(e.id()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
