package org.ahhn.com.entity;

/**
 * Created by XJX on 2016/3/12.
 */
public class Employee {
	private Integer id;
	private String name;
	private double salary;
	private String email;

	private Department department;

	public Employee() {
	}

	public Employee(double salary, String email) {
		this.salary = salary;
		this.email = email;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getSalary() {
		return salary;
	}

	public void setSalary(double salary) {
		this.salary = salary;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}
}
