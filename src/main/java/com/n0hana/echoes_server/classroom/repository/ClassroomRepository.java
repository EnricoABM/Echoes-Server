package com.n0hana.echoes_server.classroom.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.n0hana.echoes_server.classroom.model.Classroom;
import com.n0hana.echoes_server.user.User;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, UUID> {
    Optional<Classroom> findByCode(String code);
    List<Classroom> findAllByTeacher(User teacher);
}
