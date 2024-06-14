package com.zalo.Spring_Zalo.Service;

import java.util.List;

import com.zalo.Spring_Zalo.Entities.Event;

public interface EventService {
    Event createEvent(Event event, Integer companyId);

    Event updataEvent(Event event, Integer eventId, Integer companyId);

    List<Event> getAllEvents();

    Event getEventById(Long id);
}
