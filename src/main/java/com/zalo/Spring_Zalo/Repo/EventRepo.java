package com.zalo.Spring_Zalo.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.zalo.Spring_Zalo.Entities.Event;

import java.util.List;

public interface EventRepo extends JpaRepository<Event, Integer> {

}
