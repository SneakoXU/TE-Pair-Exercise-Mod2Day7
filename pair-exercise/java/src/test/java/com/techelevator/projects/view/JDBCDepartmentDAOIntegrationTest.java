package com.techelevator.projects.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.projects.model.Department;
import com.techelevator.projects.model.jdbc.JDBCDepartmentDAO;

public class JDBCDepartmentDAOIntegrationTest {
	
	private static final String TEST_DEPARTMENT = "XYZ";
	private static final String UPDATED_DEPARTMENT = "123";
	private static final String NEW_DEPARTMENT = "ABC";
	
	private static SingleConnectionDataSource dataSource;
	private JDBCDepartmentDAO dao;
	
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
		String sqlInsertDepartment = "INSERT INTO department (name) VALUES (?)";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update(sqlInsertDepartment, TEST_DEPARTMENT);
		dao = new JDBCDepartmentDAO(dataSource);
	}

	@After
	public void rollback() throws SQLException {
		dataSource.getConnection().rollback();
	}
	
	@Test
	public void search_by_name_should_return_row() {
		List<Department> testList = dao.searchDepartmentsByName(TEST_DEPARTMENT);
		assertNotEquals(0, testList.size());
		Department testDepartment = testList.get(0);
		testDepartment.setName(UPDATED_DEPARTMENT);
		dao.saveDepartment(testDepartment);
		String sqlCheckUpdate = "SELECT name FROM department WHERE department_id = ?";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		SqlRowSet rows = jdbcTemplate.queryForRowSet(sqlCheckUpdate, testDepartment.getId());
		assertEquals(true, rows.next());
		assertEquals(UPDATED_DEPARTMENT, rows.getString(1));
		Department resultDepartment = dao.getDepartmentById(testDepartment.getId());
		assertEquals(testDepartment.getId(), resultDepartment.getId());
		assertEquals(testDepartment.getName(), resultDepartment.getName());
	}
	
	@Test
	public void created_department_should_insert() {
		Department createdDept = new Department();
		createdDept.setName(NEW_DEPARTMENT);
		createdDept = dao.createDepartment(createdDept);
		String sqlCheckUpdate = "SELECT name FROM department WHERE department_id = ?";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		SqlRowSet rows = jdbcTemplate.queryForRowSet(sqlCheckUpdate, createdDept.getId());
		assertEquals(true, rows.next());
		assertEquals(NEW_DEPARTMENT, rows.getString(1));
	}
	
	
	
	
	
	
}
