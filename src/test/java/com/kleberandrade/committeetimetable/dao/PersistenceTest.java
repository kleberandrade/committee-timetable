package com.kleberandrade.committeetimetable.dao;
import com.kleberandrade.committeetimetable.model.*;
import com.kleberandrade.committeetimetable.utils.EntityManagerUtil;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
@EnabledIfEnvironmentVariable(named="COMMITTEE_DB_TEST",matches="true")
class PersistenceTest {
    static EntityManagerFactory factory;
    @BeforeAll static void open() { factory = EntityManagerUtil.createFactory(System.getenv()); }
    @AfterAll static void close() { if (factory != null) factory.close(); }
    @Test void projectTypeCrudAndFailureRollback() {
        var dao = new ProjectTypeDAO(factory);
        var type = new ProjectType(); type.setName("Synthetic defense"); dao.create(type);
        assertNotNull(type.getId());
        assertEquals("Synthetic defense",dao.findById(type.getId()).getName());
        type.setName("Updated defense"); dao.update(type);
        assertEquals("Updated defense",dao.findById(type.getId()).getName());
        assertTrue(dao.findAll().stream().anyMatch(t -> t.getId().equals(type.getId())));
        var project = new Project(); project.setTitle("Synthetic project"); project.setType(type);
        var projects = new ProjectDAO(factory); projects.create(project);
        assertThrows(PersistenceException.class, () -> dao.deleteById(type.getId()));
        assertNotNull(dao.findById(type.getId()));
        projects.delete(project); dao.delete(type);
        assertNull(dao.findById(type.getId()));
    }
    @Test void roundTripsLegacyRelationshipsAndTimes() {
        var em = factory.createEntityManager();
        try {
            em.getTransaction().begin();
            var course = new Course(); course.setName("Demo course"); em.persist(course);
            var professor = new Professor(); professor.setName("Professor P01"); em.persist(professor);
            var project = new Project(); project.setTitle("Demo project"); project.setProfessor(professor); em.persist(project);
            var student = new Student(); student.setName("Student S01"); student.setCourse(course); student.setProject(project); em.persist(student);
            var committee = new Committee(); committee.setDay(1); committee.setStartTime(java.time.LocalTime.of(9,0)); committee.setEndTime(java.time.LocalTime.of(10,0)); committee.setProject(project); committee.setProfessors(List.of(professor)); em.persist(committee);
            var schedule = new Schedule(); schedule.setDay(1); schedule.setStartTime(java.time.LocalTime.of(9,0)); schedule.setEndTime(java.time.LocalTime.of(11,0)); schedule.setCourse(course); schedule.setProfessor(professor); em.persist(schedule);
            em.flush(); em.clear();
            assertEquals(java.time.LocalTime.of(9,0),em.find(Committee.class,committee.getId()).getStartTime());
            assertEquals("Student S01",em.find(Student.class,student.getId()).getName());
            assertEquals(professor.getId(),em.find(Schedule.class,schedule.getId()).getProfessor().getId());
            em.getTransaction().rollback();
        } finally { if (em.getTransaction().isActive()) em.getTransaction().rollback(); em.close(); }
    }
}
