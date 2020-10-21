package com.techelevator.projects.model.jdbc;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.projects.model.Department;
import com.techelevator.projects.model.DepartmentDAO;

public class JDBCDepartmentDAO implements DepartmentDAO {
	
	private JdbcTemplate jdbcTemplate;

	public JDBCDepartmentDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<Department> getAllDepartments() {
		List<Department> allDepartments = new ArrayList<>();
		SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT department_id, name FROM department");
		while(rows.next()){
			Department department = new Department();
			department.setId(rows.getLong(1));
			department.setName(rows.getString(2));
			allDepartments.add(department);
		}
		return allDepartments;
	}

	@Override
	public List<Department> searchDepartmentsByName(String nameSearch) {
		List<Department> nameDepartments = new ArrayList<>();
		SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT department_id, name FROM department WHERE name = ?", nameSearch);
		while(rows.next()){
			Department department = new Department();
			department.setId(rows.getLong(1));
			department.setName(rows.getString(2));
			nameDepartments.add(department);
		}
		return nameDepartments;
	}

	@Override
	public void saveDepartment(Department updatedDepartment) {
		String command = "UPDATE department SET name = ? WHERE department_id = ?";
		jdbcTemplate.update(command, updatedDepartment.getName(), updatedDepartment.getId());
	}

	@Override
	public Department createDepartment(Department newDepartment) {
		String command = "INSERT INTO department (name) VALUES (?)";
		jdbcTemplate.update(command, newDepartment.getName());
		SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT department_id FROM department WHERE name = ? ORDER BY department_id DESC", newDepartment.getName());
		if(!rows.next()){
			return null;
		}
		newDepartment.setId(rows.getLong(1));
		return newDepartment;
	}

	@Override
	public Department getDepartmentById(Long id) {
		Department department = null;
		SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT department_id, name FROM department WHERE department_id = ?", id);
		if(rows.next()){
			department = new Department();
			department.setId(rows.getLong(1));
			department.setName(rows.getString(2));
		}
		return department;
	}

}
