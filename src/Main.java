import jdbc.SimpleDataSource;
import models.Mentor;
import models.Student;
import repositories.StudentsRepository;
import repositories.StudentsRepositoryJdbcImpl;

import java.sql.*;
import java.util.List;

public class Main {

    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1111";


    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        StudentsRepository studentsRepository = new StudentsRepositoryJdbcImpl(connection);
        System.out.println(studentsRepository.findById(2L));
        System.out.println("----------------------------------------------------------");
        List<Student> students = studentsRepository.findAllByAge(19);
        for(Student s: students){
            System.out.println(s);
        }
        System.out.println("----------------------------------------------------------");

        Student student = new Student(null, "Антон", "Шеверда", 20, 904);
        studentsRepository.save(student);
        List<Student> students2 = studentsRepository.findAllByAge(20);
        for(Student s: students2){
            System.out.println(s);
        }
        System.out.println("-----------------------------------------------------------");
        Student student2 = studentsRepository.findById(1L);
        student2.setFirstName("Иван");
        student2.setLastName(null);
        studentsRepository.update(student2);
        System.out.println(student2.toString());
        System.out.println("-----------------------------------------------------------");
        StudentsRepositoryJdbcImpl repositoryJdbc = new StudentsRepositoryJdbcImpl(connection);
        List<Student> students3 = repositoryJdbc.findAll();
        for(Student s : students3){
            System.out.println(s.toString());
        }

        connection.close();
    }
}