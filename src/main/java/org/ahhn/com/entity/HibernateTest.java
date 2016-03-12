package org.ahhn.com.entity;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by XJX on 2016/3/12.
 */
public class HibernateTest {
	private SessionFactory sessionFactory;
	private Session session;
	private Transaction transaction;

	@Before
	public void init() {
		Configuration configuration = new Configuration().configure();
		ServiceRegistry serviceRegistry =
				new ServiceRegistryBuilder().applySettings(configuration.getProperties())
						.buildServiceRegistry();
		sessionFactory = configuration.buildSessionFactory(serviceRegistry);

		session = sessionFactory.openSession();
		transaction = session.beginTransaction();
	}

	@After
	public void destroy() {
		transaction.commit();
		session.close();
		sessionFactory.close();
	}

	private final int pageNum = 6;
	private final int pageSize = 10;

	@Test
	public void testLeftJoin(){
		List<Department> departments=session.createQuery("select distinct d from Department d left join d.employees").list();
		System.out.println(departments);
	}

	@Test
	public void testLeftJoinFetch(){
		List<Department> departments=session.createQuery("select distinct d from Department d left join fetch d.employees").list();
		System.out.println(departments);
	}

	@Test
	public void testQueryGroupBy() {
		Query query = session.createQuery("select min(e.salary),max(e.salary) from Employee e group by e.department having min(salary) > :minSal")
				.setDouble("minSal", 5000000);
		List<Object[]> result = query.list();
		for (Object[] objs : result)
			System.out.println(Arrays.asList(objs));

	}

	@Test//投影查询
	public void testFieldQuery2() {
		Department department = new Department();
		department.setId(20);
		List<Employee> employees = session.createQuery("select new Employee(e.salary, e.email) from Employee e where e.department = :department")
				.setEntity("department", department).list();
		System.out.println(employees);
	}

	@Test//投影查询
	public void testFieldQuery() {
		Query query = session.createQuery("select e.email, e.salary from Employee e where e.department = :department");

		Department department = new Department();
		department.setId(20);
		List<Object[]> result = query.setEntity("department", department).list();
		for (Object[] objs : result) {
			System.out.println(Arrays.asList(objs));
		}

	}

	@Test//命名查询
	public void testNamedQuery() {
		List<Employee> employees = session.getNamedQuery("emplyeeEmps").setDouble("minSal", 6000).setDouble("maxSal", 10000).list();
		System.out.println(employees);
	}

	@Test//分页查询
	public void testQueryPage() {
		List<Employee> employees = session.createQuery("from Employee").setFirstResult((pageNum - 1) * pageSize).setMaxResults(pageSize).list();
		System.out.println(employees);
	}

	@Test
	public void testHQL() {
//		String hql = "from Employee e where e.salary > ? and e.email like ?";
//		Query query = session.createQuery(hql);
//		query.setDouble(0,1).setString(1,"%b%");

		String hql = "from Employee e where e.salary > :salary and e.email like :email order by e.salary";
		Query query = session.createQuery(hql);
		query.setDouble("salary", 1).setString("email", "%b%");

		List<Employee> employees = query.list();
		System.out.println(employees);
	}
}
