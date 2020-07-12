package repositories;

import models.Student;

import java.util.List;

/**
 * 10.07.2020
 * 01. Database
 *
 * @author Sidikov Marsel (First Software Engineering Platform)
 * @version v1.0
 */
public interface StudentsRepository extends CrudRepository<Student> {
    List<Student> findAllByAge(int age);
}
