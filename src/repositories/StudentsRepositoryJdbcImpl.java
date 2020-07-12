package repositories;

import models.Mentor;
import models.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 10.07.2020
 * 01. Database
 *
 * @author Sidikov Marsel (First Software Engineering Platform)
 * @version v1.0
 */
public class StudentsRepositoryJdbcImpl implements StudentsRepository {

    //language=SQL
    private static final String SQL_SELECT_BY_ID = "select * from student where id = ";
    private static final String SQL_SELECT_BY_AGE = "select * from student where age = ";
    private static final String SQL_SELECT_ALL = "select s.id as s_id, s.first_name as s_first_name," +
            "s.last_name as s_last_name, s.age as s_age," +
            "s.group_number as s_group_number, m.id as m_id," +
            "m.first_name as m_first_name, m.last_name as m_last_name" +
            " from mentor m full outer join student s on m.student_id = s.id";
    private static final String SQL_INSERT_STUDENT = "insert into student (first_name, last_name, age, group_number)" +
            "values (?, ?, ?, ?)";
    private static final String SQL_UPDATE_STUDENT = "update student set " +
            "first_name = '%s', " +
            "last_name = '%s', " +
            "age = %d, " +
            "group_number = %d " +
            "where id = %d";

    private final Connection connection;

    public StudentsRepositoryJdbcImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Student> findAllByAge(int age) {
        Statement statement = null;
        ResultSet result = null;
        List<Student> students = new ArrayList<>();
        try {
            statement = connection.createStatement();
            result = statement.executeQuery(SQL_SELECT_BY_AGE + age);
            while (result.next()) {
                students.add(new Student(
                        result.getLong("id"),
                        result.getString("first_name"),
                        result.getString("last_name"),
                        result.getInt("age"),
                        result.getInt("group_number")
                ));
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) {
                    //ignore
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    //ignore
                }
            }
        }
        return students;
    }

    // Необходимо вытащить список всех студентов, при этом у каждого студента должен быть проставлен список менторов
    // у менторов в свою очередь ничего проставлять (кроме имени, фамилии, id не надо)
    // student1(id, firstName, ..., mentors = [{id, firstName, lastName, null}, {}, ), student2, student3
    // все сделать одним запросом
    @Override
    public List<Student> findAll() {
        Statement statement = null;
        ResultSet result = null;
        List<Student> students = new ArrayList<>();
        try {
            statement = connection.createStatement();
            result = statement.executeQuery(SQL_SELECT_ALL);
            while (result.next()) {
                Student student = new Student(result.getLong("s_id"),
                        result.getString("s_first_name"), result.getString("s_last_name"),
                        result.getInt("s_age"), result.getInt("s_group_number"));
                int index = students.indexOf(student);
                if (index >= 0) {
                    students.get(index).getMentors().add(new Mentor(result.getLong("m_id"),
                            result.getString("m_first_name"), result.getString("m_last_name")));
                } else {
                    student.setMentors(new ArrayList<>());
                    student.getMentors().add(new Mentor(result.getLong("m_id"),
                            result.getString("m_first_name"), result.getString("m_last_name")));
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) {
                    //ignore
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    //ignore
                }
            }
        }
        return students;
    }

    @Override
    public Student findById(Long id) {
        Statement statement = null;
        ResultSet result = null;

        try {
            statement = connection.createStatement();
            result = statement.executeQuery(SQL_SELECT_BY_ID + id);
            if (result.next()) {
                return new Student(
                        result.getLong("id"),
                        result.getString("first_name"),
                        result.getString("last_name"),
                        result.getInt("age"),
                        result.getInt("group_number")
                );
            } else return null;
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }

    // просто вызывается insert для сущности
    // student = Student(null, 'Марсель', 'Сидиков', 26, 915)
    // studentsRepository.save(student);
    // student = Student(3, 'Марсель', 'Сидиков', 26, 915)
    @Override
    public void save(Student entity) {
        Statement statement = null;
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_STUDENT,
                    Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, entity.getFirstName());
            preparedStatement.setString(2, entity.getLastName());
            preparedStatement.setInt(3, entity.getAge());
            preparedStatement.setInt(4, entity.getGroupNumber());

            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                    rs.next();
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    //ignore
                }
            }
        }
    }

    // для сущности, у которой задан id выполнить обновление всех полей

    // student = Student(3, 'Марсель', 'Сидиков', 26, 915)
    // student.setFirstName("Игорь")
    // student.setLastName(null);
    // studentsRepository.update(student);
    // (3, 'Игорь', null, 26, 915)

    @Override
    public void update(Student entity) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(String.format(SQL_UPDATE_STUDENT,
                    entity.getFirstName(),
                    entity.getLastName(),
                    entity.getAge(),
                    entity.getGroupNumber(),
                    entity.getId()));
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
        // ignore
    }
}
