package eu.dirk.haase.hibernate;

import eu.dirk.haase.hibernate.jdbc.HibernateConnection;
import eu.dirk.haase.model.Employee;
import eu.dirk.haase.model.EmployeeDetail;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.jdbc.Work;

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

        Catcher catcher = new Catcher();
        System.out.println("1 session1.connection().getClass():  " + System.identityHashCode(session1.connection().unwrap(HibernateConnection.class)));
        session1.doWork(catcher);
        System.out.println("catcher.connection.getClass():  " + System.identityHashCode(catcher.connection));
        System.out.println("2 session1.connection().getClass():  " + System.identityHashCode(session1.connection().unwrap(HibernateConnection.class)));

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


    static class Catcher implements Work {
        Connection connection;
        @Override
        public void execute(Connection connection) throws SQLException {
            this.connection = connection;
        }
    }
}