package org.ahhn.com.entity;

import org.ahhn.com.dao.DepartmentDao;
import org.ahhn.com.utils.HibernateUtils;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.*;
import org.hibernate.jdbc.Work;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

	public void testBatch(){
		session.doWork(new Work() {
			public void execute(Connection connection) throws SQLException {
				//通过 JDBC 原生的 API 进行操作, 效率最高, 速度最快!
			}
		});
	}

	@Test
	public void testManagerSession() {
		//开启事务
		Session session= HibernateUtils.getInstance().getSession();
		Transaction transaction=session.beginTransaction();

		Department department=new Department();
		department.setName("ABCDEFG");

		DepartmentDao dao = new DepartmentDao();
		dao.save(department);
		dao.save(department);
		dao.save(department);

		//若 Session 是由 thread 来管理的, 则在提交或回滚事务时, 已经关闭 Session 了.
		transaction.commit();
		System.out.println(session.isOpen());
	}

	private final int pageNum = 6;
	private final int pageSize = 10;

	@Test
	public void testQueryIterate() {
		Department department = (Department) session.get(Department.class, 11);
		System.out.println(department.getName());
		System.out.println(department.getEmployees());

		Query query = session.createQuery("from Employee e where e.department.id=11");
		List<Employee> employees = query.list();
		Iterator<Employee> iterator = employees.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next().getName());
		}
	}

	@Test
	public void testQueryCache() {
		Query query = session.createQuery("from Employee");
		query.setCacheable(true);

		List<Employee> employees1 = query.list();
		System.out.println(employees1);

		List<Employee> employees2 = query.list();
		System.out.println(employees2);
	}

	@Test
	public void testCollectionSecondLevelCache() {
		Department department1 = (Department) session.get(Department.class, 11);
		Set<Employee> employees1 = department1.getEmployees();
		for (Employee employee1 : employees1)
			System.out.println(employee1.getName());

		transaction.commit();
		session.close();

		session = sessionFactory.openSession();
		transaction = session.beginTransaction();

		Department department2 = (Department) session.get(Department.class, 11);
		Set<Employee> employees2 = department2.getEmployees();
		for (Employee employee2 : employees2)
			System.out.println(employee2.getName());
	}

	@Test
	public void testSecondLevelCache() {
		Employee employee1 = (Employee) session.get(Employee.class, 1);
		System.out.println(employee1.getName());

		transaction.commit();
		session.close();

		session = sessionFactory.openSession();
		transaction = session.beginTransaction();

		Employee employee2 = (Employee) session.get(Employee.class, 1);
		System.out.println(employee2.getName());
	}

	@Test
	public void testNativeSQL() {
		Query query = session.createSQLQuery("INSERT into department VALUES (?,?)")
				.setInteger(0, 11)
				.setString(1, "AA部门");
		query.executeUpdate();
	}

	@Test //排序翻页查询
	public void testQBC4() {
		Criteria criteria = session.createCriteria(Employee.class);
		criteria.addOrder(Order.asc("salary")).addOrder(Order.desc("email"))
				.setFirstResult((pageNum - 1) * pageSize)
				.setMaxResults(pageSize);
		System.out.println(criteria.list());
	}

	@Test//统计查询: 使用 Projection 来表示: 可以由 Projections 的静态方法得到
	public void testQBC3() {
		Criteria criteria = session.createCriteria(Employee.class);
		criteria.setProjection(Projections.max("salary"));
		System.out.println(criteria.uniqueResult());
	}

	@Test
	public void testQBC2AndOrEffect() {
		Criteria criteria = session.createCriteria(Employee.class);
		//1. AND: 使用 Conjunction 表示
		//Conjunction 本身就是一个 Criterion 对象
		//且其中还可以添加 Criterion 对象
		Conjunction conjunction = new Conjunction();
		conjunction.add(Restrictions.like("name", "AAA"));
		Department department = new Department();
		department.setId(10);
		conjunction.add(Restrictions.eq("department", department));
		System.out.println(conjunction);

		//2、OR
		Disjunction disjunction = Restrictions.disjunction();
		disjunction.add(Restrictions.ge("salary", 1000.0));
		disjunction.add(Restrictions.isNull("email"));
		System.out.println(disjunction);

		criteria.add(conjunction);
		criteria.add(disjunction);
		System.out.println(criteria.list());

	}

	@Test
	public void testQBC() {
		//1. 创建一个 Criteria 对象
		Criteria criteria = session.createCriteria(Employee.class);

		//2. 添加查询条件: 在 QBC 中查询条件使用 Criterion 来表示
		//Criterion 可以通过 Restrictions 的静态方法得到
		criteria.add(Restrictions.eq("email", "aaa@aaa.com"));
		criteria.add(Restrictions.gt("salary", 1000.0));

		//3. 执行查询
		Employee employee = (Employee) criteria.uniqueResult();
		System.out.println(employee);
	}

	@Test
	public void testLeftJoin() {
		List<Department> departments = session.createQuery("select distinct d from Department d left join d.employees").list();
		System.out.println(departments);
	}

	@Test
	public void testLeftJoinFetch() {
		List<Department> departments = session.createQuery("select distinct d from Department d left join fetch d.employees").list();
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
