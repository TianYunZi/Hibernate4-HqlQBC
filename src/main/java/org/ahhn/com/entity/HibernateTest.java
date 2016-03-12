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
