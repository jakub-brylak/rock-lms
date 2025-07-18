package com.jbs.rocklms.service;

import com.jbs.rocklms.entity.Course;
import com.jbs.rocklms.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {
    
    private final CourseRepository courseRepository;
    
    @Autowired
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }
    
    public List<Course> getAllCourses(Course.CourseStatus status) {
        return courseRepository.findAllWithOptionalStatus(status);
    }
    
    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }
    
    public Course createCourse(String title, String description, Integer duration) {
        Course course = new Course(title, description, duration);
        return courseRepository.save(course);
    }
    
    public Course updateCourse(Long id, String title, String description, Integer duration) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        if (!canBeEdited(course)) {
            throw new IllegalStateException("Cannot edit archived course");
        }
        
        if (title != null) course.setTitle(title);
        if (description != null) course.setDescription(description);
        if (duration != null) course.setDuration(duration);
        
        return courseRepository.save(course);
    }
    
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }
    
    public Course publishCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        validateCourseForPublication(course);
        
        publish(course);
        return courseRepository.save(course);
    }
    
    private void validateCourseForPublication(Course course) {
        if (course.getTitle() == null || course.getTitle().trim().isEmpty()) {
            throw new IllegalStateException("Cannot publish course: title is required");
        }
        if (course.getDuration() == null || course.getDuration() < 1) {
            throw new IllegalStateException("Cannot publish course: duration must be greater than 0");
        }
        if (course.getStatus() == Course.CourseStatus.ARCHIVED) {
            throw new IllegalStateException("Cannot publish archived course");
        }
    }
    
    public Course archiveCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        course.setStatus(Course.CourseStatus.ARCHIVED);
        return courseRepository.save(course);
    }
    
    private void publish(Course course) {
        course.setStatus(Course.CourseStatus.PUBLISHED);
        course.setPublishedAt(LocalDateTime.now());
    }
    
    private boolean canBeEdited(Course course) {
        return course.getStatus() != Course.CourseStatus.ARCHIVED;
    }
}
