

import java.sql.*;

public class Service {

    public static void createDB() {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            Statement stm = con.createStatement();
            stm.executeUpdate("DROP TABLE Department IF EXISTS");
            stm.executeUpdate("CREATE TABLE Department(ID INT PRIMARY KEY, NAME VARCHAR(255))");
            stm.executeUpdate("INSERT INTO Department VALUES(1,'Accounting')");
            stm.executeUpdate("INSERT INTO Department VALUES(2,'IT')");
            stm.executeUpdate("INSERT INTO Department VALUES(3,'HR')");

            stm.executeUpdate("DROP TABLE Employee IF EXISTS");
            stm.executeUpdate("CREATE TABLE Employee(ID INT PRIMARY KEY, NAME VARCHAR(255), DepartmentID INT)");
            stm.executeUpdate("INSERT INTO Employee VALUES(1,'Pete',1)");
            stm.executeUpdate("INSERT INTO Employee VALUES(2,'Ann',1)");

            stm.executeUpdate("INSERT INTO Employee VALUES(3,'Liz',2)");
            stm.executeUpdate("INSERT INTO Employee VALUES(4,'Tom',2)");

            stm.executeUpdate("INSERT INTO Employee VALUES(5,'Todd',3)");

        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public static void addDepartment(Department d) {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            PreparedStatement stm = con.prepareStatement("INSERT INTO Department VALUES(?,?)");
            stm.setInt(1, d.departmentID);
            stm.setString(2, d.getName());
            stm.executeUpdate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void removeDepartment(Department d) {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            PreparedStatement stm = con.prepareStatement("DELETE FROM Department WHERE ID=?");
            stm.setInt(1, d.departmentID);
            stm.executeUpdate();
            String deleteEmployeesSql = "DELETE FROM Employee WHERE DepartmentID = ?";
            PreparedStatement deleteEmployeesStmt = con.prepareStatement(deleteEmployeesSql);
            deleteEmployeesStmt.setInt(1, d.departmentID);
            int employeesDeleted = deleteEmployeesStmt.executeUpdate();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public static void addEmployee(Employee empl) {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            PreparedStatement stm = con.prepareStatement("INSERT INTO Employee VALUES(?,?,?)");
            stm.setInt(1, empl.getEmployeeId());
            stm.setString(2, empl.getName());
            stm.setInt(3, empl.getDepartmentId());
            stm.executeUpdate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void removeEmployee(Employee empl) {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            PreparedStatement stm = con.prepareStatement("DELETE FROM Employee WHERE ID=?");
            stm.setInt(1, empl.getEmployeeId());
            stm.executeUpdate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void setDepartmentForAnnToHR() {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            int hrId = getDepartmentIdByName(con, "HR");

            if (hrId == -1) {
                System.err.println("Департамент HR не найден.");
                return;
            }

            String selectSql = "SELECT ID FROM Employee WHERE NAME = ?";
            PreparedStatement selectStmt = con.prepareStatement(selectSql);
            selectStmt.setString(1, "Ann");
            ResultSet rs = selectStmt.executeQuery();

            int annCount = 0;
            while (rs.next()) {
                int employeeId = rs.getInt("ID");
                String updateSql = "UPDATE Employee SET DepartmentID = ? WHERE ID = ?";
                PreparedStatement updateStmt = con.prepareStatement(updateSql);
                updateStmt.setInt(1, hrId);
                updateStmt.setInt(2, employeeId);
                updateStmt.executeUpdate();
                annCount++;
            }

            if (annCount == 1) {
                System.out.println("Департамент для сотрудника с именем Ann успешно установлен в HR.");
            } else if (annCount > 1) {
                System.err.println("Найдено более одного сотрудника с именем Ann.");
            } else {
                System.err.println("Сотрудник с именем Ann не найден.");
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при установке департамента для Ann: " + e.getMessage());
        }
    }

    private int getDepartmentIdByName(Connection conn, String departmentName) throws SQLException {
        String sql = "SELECT ID FROM Department WHERE NAME = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, departmentName);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return rs.getInt("ID");
        }
        return -1;
    }

    public void correctEmployeeNames() {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            String selectSql = "SELECT ID, NAME FROM Employee WHERE NAME <> UPPER(NAME)";
            PreparedStatement selectStmt = con.prepareStatement(selectSql);
            ResultSet rs = selectStmt.executeQuery();

            int correctedCount = 0;
            while (rs.next()) {
                int employeeId = rs.getInt("ID");
                String originalName = rs.getString("NAME");

                if (!originalName.isEmpty() && Character.isLowerCase(originalName.charAt(0))) {
                    String correctedName = originalName.substring(0, 1).toUpperCase() + originalName.substring(1).toLowerCase();

                    String updateSql = "UPDATE Employee SET NAME = ? WHERE ID = ?";
                    PreparedStatement updateStmt = con.prepareStatement(updateSql);
                    updateStmt.setString(1, correctedName);
                    updateStmt.setInt(2, employeeId);
                    updateStmt.executeUpdate();

                    correctedCount++;
                    System.out.println("Имя сотрудника с ID " + employeeId + " изменено с \"" + originalName + "\" на \"" + correctedName + "\".");
                }
            }

            System.out.println("Количество исправленных имён: " + correctedCount);

        } catch (SQLException e) {
            System.err.println("Ошибка при исправлении имён сотрудников: " + e.getMessage());
        }
    }


    public void countEmployeesInIT() {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            int itId = getDepartmentIdByName(con, "IT");

            if (itId == -1) {
                System.err.println("Департамент IT не найден.");
                return;
            }

            String selectSql = "SELECT COUNT(*) AS count FROM Employee WHERE DepartmentID = ?";
            PreparedStatement selectStmt = con.prepareStatement(selectSql);
            selectStmt.setInt(1, itId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("Количество сотрудников в IT-отделе: " + count);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении количества сотрудников в IT-отделе: " + e.getMessage());
        }
    }

    public void deleteDepartmentAndEmployees(String departmentName) {
        try (Connection con = DriverManager.getConnection("jdbc:h2:.\\Office")) {
            int departmentId = getDepartmentIdByName(con, departmentName);

            if (departmentId == -1) {
                System.err.println("Департамент " + departmentName + " не найден.");
                return;
            }

            String deleteEmployeesSql = "DELETE FROM Employee WHERE DEPARTMENT_ID = ?";
            PreparedStatement deleteEmployeesStmt = con.prepareStatement(deleteEmployeesSql);
            deleteEmployeesStmt.setInt(1, departmentId);
            int employeesDeleted = deleteEmployeesStmt.executeUpdate();
            System.out.println("Удалено " + employeesDeleted + " сотрудников из отдела " + departmentName + ".");

            String deleteDepartmentSql = "DELETE FROM Department WHERE ID = ?";
            PreparedStatement deleteDepartmentStmt = con.prepareStatement(deleteDepartmentSql);
            deleteDepartmentStmt.setInt(1, departmentId);
            System.out.println("Удален отдел " + departmentName + ".");

        } catch (SQLException e) {
            System.err.println("Ошибка при удалении отдела и сотрудников: " + e.getMessage());
        }
    }
}

