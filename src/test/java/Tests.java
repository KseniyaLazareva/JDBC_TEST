import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class Tests {

    private Service service;

    @BeforeEach
    public void setUp() {
        service = new Service();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection con = getConnection()) {
            dropTable(con, "Employee");
            dropTable(con, "Department");

            createTable(con, "Department", "ID INT PRIMARY KEY AUTO_INCREMENT, NAME VARCHAR(255) NOT NULL");
            createTable(con, "Employee", "ID INT PRIMARY KEY AUTO_INCREMENT, NAME VARCHAR(255) NOT NULL, DepartmentID INT, FOREIGN KEY (DepartmentID) REFERENCES Department(ID)");

            insertDepartment(con, "HR");
            insertDepartment(con, "Finance");
            insertDepartment(con, "IT");

            insertEmployee(con, "Ann", 1); // ID департамента HR
            insertEmployee(con, "John", 3); // ID департамента IT
            insertEmployee(con, "Mike", 3); // ID департамента IT
            insertEmployee(con, "Sarah", 2); // ID департамента Finance
            insertEmployee(con, "ann", 1); // ID департамента HR

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSetDepartmentForAnnToHR() {
        service.setDepartmentForAnnToHR();

        try (Connection con = getConnection()) {
            int hrId = getDepartmentIdByName(con, "HR");
            assertNotEquals(-1, hrId, "Департамент HR не найден");

            int annDepartmentId = getEmployeeDepartmentIdByName(con, "Ann");
            assertEquals(hrId, annDepartmentId, "Департамент для сотрудника Ann должен быть HR");

        } catch (SQLException e) {
            fail("Ошибка при проверке установки департамента для Ann: " + e.getMessage());
        }
    }

    @Test
    public void testDeleteDepartmentAndEmployees() {
        Department department = new Department(3, "IT");
        Service.removeDepartment(department);

        try (Connection con = getConnection()) {
            assertEquals(0, getDepartmentCountByName(con, "IT"), "Отдел IT должен быть удален");
            assertEquals(0, getEmployeeCountByDepartmentId(con, 3), "Все сотрудники из отдела IT должны быть удалены");

        } catch (SQLException e) {
            fail("Ошибка при проверке удаления отдела и сотрудников: " + e.getMessage());
        }
    }

    @Test
    public void testCountEmployeesInIT() {
        try (Connection con = getConnection()) {
            int itId = getDepartmentIdByName(con, "IT");
            assertNotEquals(-1, itId, "Департамент IT не найден");

            assertEquals(2, getEmployeeCountByDepartmentId(con, itId), "Количество сотрудников в IT-отделе должно быть 2");

        } catch (SQLException e) {
            fail("Ошибка при проверке количества сотрудников в IT-отделе: " + e.getMessage());
        }
    }


    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:.\\Office");
    }

    private void dropTable(Connection con, String tableName) throws SQLException {
        String sql = "DROP TABLE IF EXISTS " + tableName;
        executeUpdate(con, sql);
    }

    private void createTable(Connection con, String tableName, String columns) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + columns + ")";
        executeUpdate(con, sql);
    }

    private void insertDepartment(Connection con, String departmentName) throws SQLException {
        String sql = "INSERT INTO Department (NAME) VALUES (?)";
        executeUpdateWithParameter(con, sql, departmentName);
    }

    private void insertEmployee(Connection con, String name, int departmentId) throws SQLException {
        String sql = "INSERT INTO Employee (NAME, DepartmentID) VALUES (?, ?)";
        executeUpdateWithParameters(con, sql, name, departmentId);
    }

    private int getDepartmentIdByName(Connection con, String departmentName) throws SQLException {
        String sql = "SELECT ID FROM Department WHERE NAME = ?";
        return executeQueryForSingleInt(con, sql, departmentName);
    }

    private int getEmployeeDepartmentIdByName(Connection con, String employeeName) throws SQLException {
        String sql = "SELECT DepartmentID FROM Employee WHERE NAME = ?";
        return executeQueryForSingleInt(con, sql, employeeName);
    }

    private int getDepartmentCountByName(Connection con, String departmentName) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM Department WHERE NAME = ?";
        return executeQueryForSingleInt(con, sql, departmentName);
    }

    private int getEmployeeCountByDepartmentId(Connection con, int departmentId) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM Employee WHERE DepartmentID = ?";
        return executeQueryForSingleInt(con, sql, departmentId);
    }

    private void executeUpdate(Connection con, String sql) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.execute();
        }
    }

    private void executeUpdateWithParameter(Connection con, String sql, String parameter) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, parameter);
            stmt.executeUpdate();
        }
    }

    private void executeUpdateWithParameters(Connection con, String sql, String name, int departmentId) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, departmentId);
            stmt.executeUpdate();
        }
    }

    private int executeQueryForSingleInt(Connection con, String sql, Object... parameters) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] instanceof String) {
                    stmt.setString(i + 1, (String) parameters[i]);
                } else if (parameters[i] instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) parameters[i]);
                }
            }
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        }
    }
}
