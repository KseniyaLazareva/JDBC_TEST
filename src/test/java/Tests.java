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

    // Метод для инициализации базы данных перед каждым тестом
    private void initializeDatabase() {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            // Удаление существующих таблиц
            String dropEmployeeTableSql = "DROP TABLE IF EXISTS Employee";
            PreparedStatement dropEmployeeStmt = con.prepareStatement(dropEmployeeTableSql);
            dropEmployeeStmt.execute();

            String dropDepartmentTableSql = "DROP TABLE IF EXISTS Department";
            PreparedStatement dropDepartmentStmt = con.prepareStatement(dropDepartmentTableSql);
            dropDepartmentStmt.execute();

            // Создание таблицы Department
            String createDepartmentTableSql = "CREATE TABLE IF NOT EXISTS Department (" +
                    "ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "NAME VARCHAR(255) NOT NULL)";
            PreparedStatement createDepartmentStmt = con.prepareStatement(createDepartmentTableSql);
            createDepartmentStmt.execute();

            // Создание таблицы Employee
            String createEmployeeTableSql = "CREATE TABLE IF NOT EXISTS Employee (" +
                    "ID INT PRIMARY KEY AUTO_INCREMENT, " +
                    "NAME VARCHAR(255) NOT NULL, " +
                    "DEPARTMENT_ID INT, " +
                    "FOREIGN KEY (DEPARTMENT_ID) REFERENCES Department(ID))";
            PreparedStatement createEmployeeStmt = con.prepareStatement(createEmployeeTableSql);
            createEmployeeStmt.execute();

            // Вставка тестовых данных в таблицу Department
            String insertDepartmentSql = "INSERT INTO Department (NAME) VALUES (?)";
            PreparedStatement insertDepartmentStmt = con.prepareStatement(insertDepartmentSql);
            insertDepartmentStmt.setString(1, "HR");
            insertDepartmentStmt.executeUpdate();
            insertDepartmentStmt.setString(1, "Finance");
            insertDepartmentStmt.executeUpdate();
            insertDepartmentStmt.setString(1, "IT");
            insertDepartmentStmt.executeUpdate();

            // Вставка тестовых данных в таблицу Employee
            String insertEmployeeSql = "INSERT INTO Employee (NAME, DEPARTMENT_ID) VALUES (?, ?)";
            PreparedStatement insertEmployeeStmt = con.prepareStatement(insertEmployeeSql);
            insertEmployeeStmt.setString(1, "Ann");
            insertEmployeeStmt.setInt(2, 1); // ID департамента HR
            insertEmployeeStmt.executeUpdate();
            insertEmployeeStmt.setString(1, "John");
            insertEmployeeStmt.setInt(2, 3); // ID департамента IT
            insertEmployeeStmt.executeUpdate();
            insertEmployeeStmt.setString(1, "Mike");
            insertEmployeeStmt.setInt(2, 3); // ID департамента IT
            insertEmployeeStmt.executeUpdate();
            insertEmployeeStmt.setString(1, "Sarah");
            insertEmployeeStmt.setInt(2, 2); // ID департамента Finance
            insertEmployeeStmt.executeUpdate();
            insertEmployeeStmt.setString(1, "ann");
            insertEmployeeStmt.setInt(2, 1); // ID департамента HR
            insertEmployeeStmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSetDepartmentForAnnToHR() {
        service.setDepartmentForAnnToHR();

        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            // Получение ID департамента HR
            String getHrIdSql = "SELECT ID FROM Department WHERE NAME = ?";
            PreparedStatement getHrIdStmt = con.prepareStatement(getHrIdSql);
            getHrIdStmt.setString(1, "HR");
            ResultSet hrIdRs = getHrIdStmt.executeQuery();

            int hrId = -1;
            if (hrIdRs.next()) {
                hrId = hrIdRs.getInt("ID");
            }

            // Проверка, что департамент HR найден
            assertNotEquals(-1, hrId, "Департамент HR не найден");

            // Проверка, что департамент для сотрудника Ann установлен в HR
            String selectSql = "SELECT DEPARTMENT_ID FROM Employee WHERE NAME = ?";
            PreparedStatement selectStmt = con.prepareStatement(selectSql);
            selectStmt.setString(1, "Ann");
            ResultSet rs = selectStmt.executeQuery();

            int departmentId = -1;
            while (rs.next()) {
                departmentId = rs.getInt("DEPARTMENT_ID");
            }

            assertEquals(hrId, departmentId, "Департамент для сотрудника Ann должен быть HR");

        } catch (SQLException e) {
            fail("Ошибка при проверке установки департамента для Ann: " + e.getMessage());
        }
    }

    @Test
    public void testDeleteDepartmentAndEmployees() {

        service.deleteDepartmentAndEmployees("IT");

        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            // Проверка, что отдел IT удален
            String selectDepartmentSql = "SELECT COUNT(*) AS count FROM Department WHERE NAME = ?";
            PreparedStatement selectDepartmentStmt = con.prepareStatement(selectDepartmentSql);
            selectDepartmentStmt.setString(1, "IT");
            ResultSet rs = selectDepartmentStmt.executeQuery();

            int departmentCount = 0;
            if (rs.next()) {
                departmentCount = rs.getInt("count");
            }

            assertEquals(0, departmentCount, "Отдел IT должен быть удален");

            // Проверка, что все сотрудники из отдела IT удалены
            String selectEmployeeSql = "SELECT COUNT(*) AS count FROM Employee WHERE DEPARTMENT_ID = ?";
            PreparedStatement selectEmployeeStmt = con.prepareStatement(selectEmployeeSql);
            selectEmployeeStmt.setInt(1, 3); // Предполагаемый ID отдела IT
            rs = selectEmployeeStmt.executeQuery();

            int employeeCount = 0;
            if (rs.next()) {
                employeeCount = rs.getInt("count");
            }

            assertEquals(0, employeeCount, "Все сотрудники из отдела IT должны быть удалены");

        } catch (SQLException e) {
            fail("Ошибка при проверке удаления отдела и сотрудников: " + e.getMessage());
        }
    }

    @Test
    public void testCountEmployeesInIT() {
        service.countEmployeesInIT();

        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            // Получение ID департамента IT
            String getItIdSql = "SELECT ID FROM Department WHERE NAME = ?";
            PreparedStatement getItIdStmt = con.prepareStatement(getItIdSql);
            getItIdStmt.setString(1, "IT");
            ResultSet itIdRs = getItIdStmt.executeQuery();

            int itId = -1;
            if (itIdRs.next()) {
                itId = itIdRs.getInt("ID");
            }

            // Проверка, что департамент IT найден
            assertNotEquals(-1, itId, "Департамент IT не найден");

            // Проверка количества сотрудников в IT-отделе
            String selectSql = "SELECT COUNT(*) AS count FROM Employee WHERE DEPARTMENT_ID = ?";
            PreparedStatement selectStmt = con.prepareStatement(selectSql);
            selectStmt.setInt(1, itId);
            ResultSet rs = selectStmt.executeQuery();

            int count = 0;
            if (rs.next()) {
                count = rs.getInt("count");
            }

            assertEquals(2, count, "Количество сотрудников в IT-отделе должно быть 2");

        } catch (SQLException e) {
            fail("Ошибка при проверке количества сотрудников в IT-отделе: " + e.getMessage());
        }
    }
}