package com.techelevator.projects.model.jdbc;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.projects.model.Department;
import com.techelevator.projects.model.Project;
import com.techelevator.projects.model.ProjectDAO;

public class JDBCProjectDAO implements ProjectDAO {

	private JdbcTemplate jdbcTemplate;

	public JDBCProjectDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public List<Project> getAllActiveProjects() {
		List<Project> allActiveProjects = new ArrayList<>();
		SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT project_id, name, from_date, to_date FROM project WHERE current_date BETWEEN from_date AND to_date");
		while(rows.next()){
			Project project = new Project();
			project.setId(rows.getLong(1));
			project.setName(rows.getString(2));
			project.setStartDate(rows.getDate(3).toLocalDate());
			project.setEndDate(rows.getDate(4).toLocalDate());
			allActiveProjects.add(project);
		}
		return allActiveProjects;
	}

	@Override
	public void removeEmployeeFromProject(Long projectId, Long employeeId) {
		String command = "DELETE FROM project_employee WHERE project_id = ? AND employee_id = ?";
		jdbcTemplate.update(command, projectId, employeeId);
	}

	@Override
	public void addEmployeeToProject(Long projectId, Long employeeId) {
		SqlRowSet rows = jdbcTemplate.queryForRowSet("SELECT * FROM project_employee WHERE project_id = ? AND employee_id = ?", projectId, employeeId);
		if(!rows.next()){
			String command = "INSERT INTO project_employee (project_id, employee_id) VALUES (?, ?)";
			jdbcTemplate.update(command, projectId, employeeId);
		}
	}

}
