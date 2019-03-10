package eu.dirk.haase.hibernate;

import eu.dirk.haase.model.Employee;
import eu.dirk.haase.model.EmployeeDetail;
import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws SQLException {

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session session1 = sf.openSession();
        session1.beginTransaction();

        EmployeeDetail employeeDetail = new EmployeeDetail("10th Street", "LA", "San Francisco", "U.S.");

        Employee employee = new Employee("Nina", "Mayers", new Date(121212),
                "114-857-965");
        employee.setEmployeeDetail(employeeDetail);
        employeeDetail.setEmployee(employee);

        session1.save(employee);

        List<Employee> employees = session1.createQuery("from Employee").list();
        for (Employee employee1 : employees) {
            System.out.println(employee1.getFirstname() + " , "
                    + employee1.getLastname() + ", "
                    + employee1.getEmployeeDetail().getState());
        }

        session1.flush();
        session1.getTransaction().commit();
        session1.close();
    }

}