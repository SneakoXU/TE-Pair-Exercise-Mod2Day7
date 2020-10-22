package com.techelevator.projects.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.projects.model.Employee;
import com.techelevator.projects.model.jdbc.JDBCEmployeeDAO;

public class JDBCEmployeeDAOIntegrationTest {

	private static final String TEST_EMPLOYEE_FIRST = "XYZ";
	private static final String TEST_EMPLOYEE_LAST = "ZYX";
	private static final char TEST_EMPLOYEE_GENDER = 'M';
	private static final LocalDate TEST_EMPLOYEE_HIREDATE = LocalDate.now();
	private static final LocalDate TEST_EMPLOYEE_BIRTHDATE = LocalDate.now();

	private static final String TEST_PROJECT = "XYZPROJ";
	
	private static final String TEST_DEPT_A = "XYZDEPT";
	private static final String TEST_DEPT_B = "ZYXDEPT";
	
	private static SingleConnectionDataSource dataSource;
	private JDBCEmployeeDAO dao;
	
	private static Long deptAId;
	private static Long deptBId;
	
	private static Employee target;
	
	@BeforeClass
	public static void setupDataSource() {
		dataSource = new SingleConnectionDataSource();
		dataSource.setUrl("jdbc:postgresql://localhost:5432/projects");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres1");
		
		dataSource.setAutoCommit(false);
	}
	
	@AfterClass
	public static void closeDataSource() throws SQLException {
		dataSource.destroy();
	}
	
	@Before
	public void setup() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	
		String sqlInsertDepartment = "INSERT INTO department (name) VALUES (?)";
		jdbcTemplate.update(sqlInsertDepartment, TEST_DEPT_A);
		jdbcTemplate.update(sqlInsertDepartment, TEST_DEPT_B);

		SqlRowSet depts = jdbcTemplate.queryForRowSet("SELECT department_id FROM department WHERE name = ?", TEST_DEPT_A);
		depts.next();
		deptAId = depts.getLong(1);
		depts = jdbcTemplate.queryForRowSet("SELECT department_id FROM department WHERE name = ?", TEST_DEPT_B);
		depts.next();
		deptBId = depts.getLong(1);
		
		String sqlInsertEmployee = "INSERT INTO employee (department_id, first_name, last_name, birth_date, gender, hire_date) VALUES (?, ?, ?, ?, ?, ?)";
		jdbcTemplate.update(sqlInsertEmployee, deptAId, TEST_EMPLOYEE_FIRST, TEST_EMPLOYEE_LAST, TEST_EMPLOYEE_BIRTHDATE, TEST_EMPLOYEE_GENDER, TEST_EMPLOYEE_HIREDATE);
		
		SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT employee_id FROM employee WHERE department_id = ?", deptAId);
		rows.next();
		target = new Employee();
		target.setId(rows.getLong(1));
		target.setDepartmentId(deptAId);
		target.setFirstName(TEST_EMPLOYEE_FIRST);
		target.setLastName(TEST_EMPLOYEE_LAST);
		target.setBirthDay(TEST_EMPLOYEE_BIRTHDATE);
		target.setGender(TEST_EMPLOYEE_GENDER);
		target.setHireDate(TEST_EMPLOYEE_HIREDATE);
		
		String sqlInsertProject = "INSERT INTO project (name) VALUES (?)";
		jdbcTemplate.update(sqlInsertProject, TEST_PROJECT);
		
		dao = new JDBCEmployeeDAO(dataSource);
	}

	@After
	public void rollback() throws SQLException {
		dataSource.getConnection().rollback();
	}
	
	@Test
	public void search_by_name_should_return_row(){
		List<Employee> results = dao.searchEmployeesByName(TEST_EMPLOYEE_FIRST, TEST_EMPLOYEE_LAST);
		assertNotEquals(0, results.size());
		boolean foundTarget = false;
		for(Employee employee : results){
			if(employee.getId() == target.getId()){
				foundTarget = true;
				break;
			}
		}
		assertEquals(true, foundTarget);
	}
	
	@Test
	public void get_without_projects_should_return_row() {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		SqlRowSet projects = jdbcTemplate.queryForRowSet("SELECT project_id FROM project WHERE name = ?", TEST_PROJECT);
		assertEquals("If it fails here, JdbcTemplate is broken.", true, projects.next());
		long projectId = projects.getLong(1);
		
		List<Employee> results = dao.getEmployeesWithoutProjects();
		assertNotEquals(0, results.size());
		
		boolean targetFound = false;
		for(Employee employee : results){
			if(employee.getId() == target.getId()){
				targetFound = true;
				break;
			}
		}
		assertEquals(true, targetFound);

		jdbcTemplate.update("INSERT INTO project_employee (project_id, employee_id) VALUES (?, ?)", projectId, target.getId());
		
		results = dao.getEmployeesWithoutProjects();
		assertNotEquals(0, results.size());
		
		targetFound = false;
		for(Employee employee : results){
			if(employee.getId() == target.getId()){
				targetFound = true;
				break;
			}
		}
		assertEquals(false, targetFound);
		
		results = dao.getEmployeesByProjectId(projectId);
		assertNotEquals(0, results.size());
		
		targetFound = false;
		for(Employee employee : results){
			if(employee.getId() == target.getId()){
				targetFound = true;
				break;
			}
		}
		assertEquals(true, targetFound);
	}
	
	@Test
	public void change_and_get_by_department_test() {
		List<Employee> results = dao.getEmployeesByDepartmentId(deptBId);
		assertEquals(0, results.size());
		results = dao.getEmployeesByDepartmentId(deptAId);
		assertNotEquals(0, results.size());
		Employee targetEmployee = results.get(0);
		Long empId = targetEmployee.getId();
		
		dao.changeEmployeeDepartment(empId, deptBId);
		
		results = dao.getEmployeesByDepartmentId(deptAId);
		assertEquals(0, results.size());
		results = dao.getEmployeesByDepartmentId(deptBId);
		assertNotEquals(0, results.size());
		targetEmployee = results.get(0);
		assertEquals(empId, targetEmployee.getId());
	}
	
}
