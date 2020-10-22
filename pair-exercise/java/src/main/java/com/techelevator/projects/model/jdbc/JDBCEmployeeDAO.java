package com.techelevator.projects.model.jdbc;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.projects.model.Department;
import com.techelevator.projects.model.Employee;
import com.techelevator.projects.model.Employee;
import com.techelevator.projects.model.EmployeeDAO;

public class JDBCEmployeeDAO implements EmployeeDAO {

	private JdbcTemplate jdbcTemplate;

	public JDBCEmployeeDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public List<Employee> getAllEmployees() {
		List<Employee> allEmployees = new ArrayList<>();
		SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT employee_id, department_id, first_name, last_name, birth_date, gender, hire_date FROM employee");
		while(rows.next()){
			Employee employee = new Employee();
			employee.setId(rows.getLong(1));
			employee.setDepartmentId(rows.getLong(2));
			employee.setFirstName(rows.getString(3));
			employee.setLastName(rows.getString(4));
			employee.setBirthDay(rows.getDate(5).toLocalDate());
			employee.setGender(rows.getString(6).charAt(0));
			employee.setHireDate(rows.getDate(7).toLocalDate());
			allEmployees.add(employee);
		}
		return allEmployees;
	}

	@Override
	public List<Employee> searchEmployeesByName(String firstNameSearch, String lastNameSearch) {
		List<Employee> nameEmployees = new ArrayList<>();
		SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT employee_id, department_id, first_name, last_name, birth_date, gender, hire_date FROM employee WHERE first_name = ? AND last_name = ?", firstNameSearch, lastNameSearch);
		while(rows.next()){
			Employee employee = new Employee();
			employee.setId(rows.getLong(1));
			employee.setDepartmentId(rows.getLong(2));
			employee.setFirstName(rows.getString(3));
			employee.setLastName(rows.getString(4));
			employee.setBirthDay(rows.getDate(5).toLocalDate());
			employee.setGender(rows.getString(6).charAt(0));
			employee.setHireDate(rows.getDate(7).toLocalDate());
			nameEmployees.add(employee);
		}
		return nameEmployees;
	}

	@Override
	public List<Employee> getEmployeesByDepartmentId(long id) {
		List<Employee> employeeByName = new ArrayList<>();
		SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT employee_id, department_id, first_name, last_name, birth_date, gender, hire_date FROM employee WHERE id = ?", id);
		while(rows.next()){
			Employee employee = new Employee();
			employee.setId(rows.getLong(1));
			employee.setDepartmentId(rows.getLong(2));
			employee.setFirstName(rows.getString(3));
			employee.setLastName(rows.getString(4));
			employee.setBirthDay(rows.getDate(5).toLocalDate());
			employee.setGender(rows.getString(6).charAt(0));
			employee.setHireDate(rows.getDate(7).toLocalDate());
			employeeByName.add(employee);
		}
		return employeeByName;
	}

	@Override
	public List<Employee> getEmployeesWithoutProjects() {
		List<Employee> employeesWithoutProjects = new ArrayList<>();
		SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT e.employee_id, e.department_id, e.first_name, e.last_name, e.birth_date, e.gender, e.hire_date "
				+ "FROM employee as e LEFT OUTER JOIN project_employee as pe ON e.employee_id = pe.employee_id");
		while(rows.next()){
			Employee employee = new Employee();
			employee.setId(rows.getLong(1));
			employee.setDepartmentId(rows.getLong(2));
			employee.setFirstName(rows.getString(3));
			employee.setLastName(rows.getString(4));
			employee.setBirthDay(rows.getDate(5).toLocalDate());
			employee.setGender(rows.getString(6).charAt(0));
			employee.setHireDate(rows.getDate(7).toLocalDate());
			employeesWithoutProjects.add(employee);
		}
		
		return employeesWithoutProjects;
	}

	@Override
	public List<Employee> getEmployeesByProjectId(Long projectId) {
		List<Employee> employeesProjects = new ArrayList<>();
		SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT e.employee_id, e.department_id, e.first_name, e.last_name, e.birth_date, e.gender, e.hire_date "
				+ "FROM employee as e INNER JOIN project_employee as pe ON e.employee_id = pe.employee_id WHERE pe.project_id = ?", projectId);
		while(rows.next()){
			Employee employee = new Employee();
			employee.setId(rows.getLong(1));
			employee.setDepartmentId(rows.getLong(2));
			employee.setFirstName(rows.getString(3));
			employee.setLastName(rows.getString(4));
			employee.setBirthDay(rows.getDate(5).toLocalDate());
			employee.setGender(rows.getString(6).charAt(0));
			employee.setHireDate(rows.getDate(7).toLocalDate());
			employeesProjects.add(employee);
		}
		
		return employeesProjects;
	}
		

	@Override
	public void changeEmployeeDepartment(Long employeeId, Long departmentId) {
		String command = "UPDATE employee SET department_id = ? WHERE employee_id = ?";
		jdbcTemplate.update(command, departmentId, employeeId);
		
		
	}

}
