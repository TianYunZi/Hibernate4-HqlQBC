package org.ahhn.com.dao;

import org.ahhn.com.entity.Department;
import org.ahhn.com.utils.HibernateUtils;
import org.hibernate.Session;

/**
 * Created by XJX on 2016/3/13.
 */
public class DepartmentDao {
	public void save(Department department){
		Session session= HibernateUtils.getInstance().getSession();
		System.out.println(session.hashCode());
		session.save(department);
	}
}
