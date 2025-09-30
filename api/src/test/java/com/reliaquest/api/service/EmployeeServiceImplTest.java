package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeClient;
import com.reliaquest.api.dto.ApiListResponse;
import com.reliaquest.api.dto.ApiSingleResponse;
import com.reliaquest.api.dto.Employee;
import com.reliaquest.api.dto.EmployeeCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock private EmployeeClient client;

    @InjectMocks private EmployeeServiceImpl service;

    // ------------ helpers ------------

    private static Employee emp(String name, int salary) {
        return new Employee(
                UUID.randomUUID().toString(),
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
        var e1 = emp("Alice", 120_000);
        var e2 = emp("Bob", 90_000);
        given(client.getAll()).willReturn(listResp(List.of(e1, e2)));

        var result = service.getAllEmployees();

        assertThat(result).hasSize(2).containsExactly(e1, e2);
    }

    @Test
    @DisplayName("getAllEmployees returns empty when client/data is null")
    void getAllEmployees_emptyWhenNull() {
        given(client.getAll()).willReturn(null);

        var result = service.getAllEmployees();

        assertThat(result).isEmpty();
    }

    // ------------ getEmployeesByNameSearch ------------

    @Test
    @DisplayName("search filters by lowercase contains")
    void search_filters() {
        var a = emp("Rosario O'Kon", 130_000);
        var b = emp("Jane Doe", 110_000);
        given(client.getAll()).willReturn(listResp(List.of(a, b)));

        var result = service.getEmployeesByNameSearch("rosa");

        assertThat(result).containsExactly(a);
    }

    @Test
    @DisplayName("search with blank returns all")
    void search_blankReturnsAll() {
        var a = emp("Alice", 1);
        var b = emp("Bob", 2);
        given(client.getAll()).willReturn(listResp(List.of(a, b)));

        assertThat(service.getEmployeesByNameSearch(null)).containsExactly(a, b);
        assertThat(service.getEmployeesByNameSearch("")).containsExactly(a, b);
    }

    // ------------ getEmployeeById ------------

    @Test
    @DisplayName("getById returns employee when client returns data")
    void getById_ok() {
        var e = emp("Jane Doe", 123_000);
        given(client.getById(e.id())).willReturn(oneResp(e));

        var out = service.getEmployeeById(e.id());

        assertThat(out).isEqualTo(e);
    }


    @Test
    @DisplayName("getById throws 404 when client returns null data")
    void getById_notFound_nullData() {
        var id = UUID.randomUUID().toString();
        given(client.getById(id)).willReturn(new ApiSingleResponse<>(null, "ok"));

        assertThatThrownBy(() -> service.getEmployeeById(id))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ------------ getHighestSalaryOfEmployees ------------

    @Test
    @DisplayName("highestSalary returns max salary")
    void highestSalary_ok() {
        given(client.getAll())
                .willReturn(listResp(List.of(emp("A", 50), emp("B", 70), emp("C", 60))));

        int max = service.getHighestSalaryOfEmployees();

        assertThat(max).isEqualTo(70);
    }

    @Test
    @DisplayName("highestSalary returns 0 when empty")
    void highestSalary_empty() {
        given(client.getAll()).willReturn(listResp(List.of()));

        assertThat(service.getHighestSalaryOfEmployees()).isZero();
    }

    // ------------ getTop10HighestEarningEmployeeNames ------------

    @Test
    @DisplayName("top10 returns names of top 10 by salary, desc")
    void top10_ok() {
        var list = List.of(
                emp("N1", 10), emp("N2", 20), emp("N3", 30), emp("N4", 40), emp("N5", 50),
                emp("N6", 60), emp("N7", 70), emp("N8", 80), emp("N9", 90), emp("N10", 100),
                emp("N11", 5), emp("N12", 1)
        );
        given(client.getAll()).willReturn(listResp(list));

        var names = service.getTop10HighestEarningEmployeeNames();

        assertThat(names).hasSize(10);
        assertThat(names.get(0)).isEqualTo("N10");
        assertThat(names.get(9)).isEqualTo("N1");
    }

    // ------------ createEmployee ------------

    @Test
    @DisplayName("create returns created employee")
    void create_ok() {
        var req = new EmployeeCreateRequest("Jane Doe", 120_000, 33, "Senior SWE");
        var created = emp("Jane Doe", 120_000);
        given(client.create(req)).willReturn(oneResp(created));

        var out = service.createEmployee(req);

        assertThat(out).isEqualTo(created);
    }

    @Test
    @DisplayName("create throws 500 when client returns null/empty")
    void create_failNull() {
        var req = new EmployeeCreateRequest("Jane Doe", 120_000, 33, "Senior SWE");
        given(client.create(req)).willReturn(null);

        assertThatThrownBy(() -> service.createEmployee(req))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ------------ deleteEmployeeById ------------

    @Test
    @DisplayName("delete by id returns deleted NAME when downstream succeeds")
    void delete_ok() {
        var e = emp("Jane Doe", 100);
        given(client.getById(e.id())).willReturn(oneResp(e));
        given(client.deleteByName("Jane Doe")).willReturn(true);

        var out = service.deleteEmployeeById(e.id());

        assertThat(out).isEqualTo("Jane Doe");
        then(client).should().deleteByName("Jane Doe");
    }

    @Test
    @DisplayName("delete by id -> 404 when deleteByName returns false")
    void delete_deleteByNameFalse() {
        var e = emp("Ghost Name", 10);
        given(client.getById(e.id())).willReturn(oneResp(e));
        given(client.deleteByName("Ghost Name")).willReturn(false);

        assertThatThrownBy(() -> service.deleteEmployeeById(e.id()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
