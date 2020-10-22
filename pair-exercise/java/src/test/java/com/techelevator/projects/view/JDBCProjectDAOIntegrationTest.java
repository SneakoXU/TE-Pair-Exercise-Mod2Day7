package com.techelevator.projects.view;

import static org.junit.Assert.assertEquals;

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

import com.techelevator.projects.model.Project;
import com.techelevator.projects.model.jdbc.JDBCProjectDAO;

public class JDBCProjectDAOIntegrationTest {

	private static final String TEST_PROJECT_A = "XYZ";
	private static final String TEST_PROJECT_B = "ZYX";
	private static final LocalDate NOW = LocalDate.now();
	
	private static SingleConnectionDataSource dataSource;
	private JDBCProjectDAO dao;
	
	private static Project targetA;
	private static Project targetB;
	
	private static final Long TEST_EMPLOYEE_DEPARTMENT = 1L;
	private static final String TEST_EMPLOYEE_FIRST = "XYZ";
	private static final String TEST_EMPLOYEE_LAST = "ZYX";
	private static final char TEST_EMPLOYEE_GENDER = 'M';
	private static final LocalDate TEST_EMPLOYEE_HIREDATE = LocalDate.now();
	private static final LocalDate TEST_EMPLOYEE_BIRTHDATE = LocalDate.now();

	
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
		String sqlInsertProject = "INSERT INTO project (name, from_date, to_date) VALUES (?, ? - 10, ? + 10)";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sqlInsertProject, TEST_PROJECT_A, NOW, NOW);
		sqlInsertProject = "INSERT INTO project (name, from_date, to_date) VALUES (?, ? + 10, ? + 20)";
		jdbcTemplate.update(sqlInsertProject, TEST_PROJECT_B, NOW, NOW);
		dao = new JDBCProjectDAO(dataSource);
		
		String sqlSearchProject = "SELECT project_id, from_date, to_date FROM project WHERE name = ?";
		SqlRowSet rows = jdbcTemplate.queryForRowSet(sqlSearchProject, TEST_PROJECT_A);
		rows.next();
		targetA = new Project();
		targetA.setId(rows.getLong(1));
		targetA.setName(TEST_PROJECT_A);
		targetA.setStartDate(rows.getDate(2).toLocalDate());
		targetA.setEndDate(rows.getDate(3).toLocalDate());
		rows = jdbcTemplate.queryForRowSet(sqlSearchProject, TEST_PROJECT_B);
		rows.next();
		targetB = new Project();
		targetB.setId(rows.getLong(1));
		targetB.setName(TEST_PROJECT_B);
		targetB.setStartDate(rows.getDate(2).toLocalDate());
		targetA.setEndDate(rows.getDate(3).toLocalDate());
	}

	@After
	public void rollback() throws SQLException {
		dataSource.getConnection().rollback();
	}
	
	@Test
	public void get_active_projects(){
		List<Project> activeProjects = dao.getAllActiveProjects();
		boolean targetAFound = false;
		boolean targetBFound = false;
		for(Project project : activeProjects){
			if(project.getId() == targetA.getId()){
				targetAFound = true;
			}
			if(project.getId() == targetB.getId()){
				targetBFound = true;
			}
		}
		assertEquals(true, targetAFound);
		assertEquals(false, targetBFound);
	}
	
	@Test
	public void add_remove_employees(){
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
	
		String sqlInsertEmployee = "INSERT INTO employee (department_id, first_name, last_name, birth_date, gender, hire_date) VALUES (?, ?, ?, ?, ?, ?)";
		jdbcTemplate.update(sqlInsertEmployee, TEST_EMPLOYEE_DEPARTMENT, TEST_EMPLOYEE_FIRST, TEST_EMPLOYEE_LAST, TEST_EMPLOYEE_BIRTHDATE, TEST_EMPLOYEE_GENDER, TEST_EMPLOYEE_HIREDATE);
		
		SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT employee_id FROM employee WHERE first_name = ?", TEST_EMPLOYEE_FIRST);
		rows.next();
		Long empId = rows.getLong(1);
		
		dao.addEmployeeToProject(targetA.getId(), empId);
		rows = jdbcTemplate.queryForRowSet("SELECT * FROM project_employee WHERE project_id = ? AND employee_id = ?", targetA.getId(), empId);
		assertEquals(true, rows.next());
		dao.removeEmployeeFromProject(targetA.getId(), empId);
		rows = jdbcTemplate.queryForRowSet("SELECT * FROM project_employee WHERE project_id = ? AND employee_id = ?", targetA.getId(), empId);
		assertEquals(false, rows.next());
	}
	
}
