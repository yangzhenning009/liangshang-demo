package com.liangshan.demo.repository;

import com.liangshan.demo.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Long> {

    List<Person> findAllByOrderByNameAsc();
}
