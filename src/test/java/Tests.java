import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;


public class Tests {

    private Service service;

    @BeforeEach
    public void setUp() {
        service = new Service();
        service.initializeDatabase();
    }

    @Test
    public void testSetDepartmentForAnnToHR() {
        service.setDepartmentForAnnToHR();

        try (var con = service.getConnection()) {
            int hrId = service.getDepartmentIdByName(con, "HR");
            assertNotEquals(-1, hrId, "Департамент HR не найден");

            int annDepartmentId = service.getEmployeeDepartmentIdByName(con, "Ann");
            assertEquals(hrId, annDepartmentId, "Департамент для сотрудника Ann должен быть HR");

        } catch (SQLException e) {
            fail("Ошибка при проверке установки департамента для Ann: " + e.getMessage());
        }
    }

    @Test
    public void testDeleteDepartmentAndEmployees() {
        Department department = new Department(3, "IT");
        Service.removeDepartment(department);

        try (var con = service.getConnection()) {
            assertEquals(0, service.getDepartmentCountByName(con, "IT"), "Отдел IT должен быть удален");
            assertEquals(0, service.getEmployeeCountByDepartmentId(con, 3), "Все сотрудники из отдела IT должны быть удалены");

        } catch (SQLException e) {
            fail("Ошибка при проверке удаления отдела и сотрудников: " + e.getMessage());
        }
    }

    @Test
    public void testCountEmployeesInIT() {
        try (var con = service.getConnection()) {
            int itId = service.getDepartmentIdByName(con, "IT");
            assertNotEquals(-1, itId, "Департамент IT не найден");

            assertEquals(2, service.getEmployeeCountByDepartmentId(con, itId), "Количество сотрудников в IT-отделе должно быть 2");

        } catch (SQLException e) {
            fail("Ошибка при проверке количества сотрудников в IT-отделе: " + e.getMessage());
        }
    }
}
