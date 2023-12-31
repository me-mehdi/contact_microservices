
package org.daypilot.demo.html5eventcalendarspring.controller;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.daypilot.demo.html5eventcalendarspring.domain.Event;
import org.daypilot.demo.html5eventcalendarspring.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@RestController
public class CalendarController {

    private static final Logger logger = LoggerFactory.getLogger(CalendarController.class);  // Declare the logger

    @Autowired
    private EventRepository eventRepository;

    @RequestMapping("/api")
    @ResponseBody
    public String home() {
        return "Welcome!";
    }

    @GetMapping("/api/events")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    public Iterable<Event> getEvents(
            @RequestParam("start") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime end,
            @RequestParam("userId") Long userId
    ) {
        logger.info("Received request - start: {}, end: {}, userId: {}", start, end, userId);
        return eventRepository.findBetweenAndUserId(start, end, userId);
    }



    @PostMapping("/api/events/create")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Transactional
    public Event createEvent(@RequestBody EventCreateParams params) {

        Event e = new Event();
        e.setStart(params.start);
        e.setEnd(params.end);
        e.setText(params.text);
        e.setUserId(params.userId);
        eventRepository.save(e);

        return e;
    }

    @PostMapping("/api/events/move")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Transactional
    public Event moveEvent(@RequestBody EventMoveParams params) {
        Event event = eventRepository.findById(params.id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        event.setStart(params.start);
        event.setEnd(params.end);
        eventRepository.save(event);
        return event;
    }

    @PostMapping("/api/events/setColor")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Transactional
    public Event setColor(@RequestBody SetColorParams params) {
        Event event = eventRepository.findById(params.id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        event.setColor(params.color);
        eventRepository.save(event);
        return event;
    }

    @PostMapping("/api/events/delete")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @Transactional
    public EventDeleteResponse deleteEvent(@RequestBody EventDeleteParams params) {
        eventRepository.deleteById(params.id);
        return new EventDeleteResponse("Deleted");
    }

    public static class EventDeleteParams {
        public Long id;
    }

    public static class EventDeleteResponse {
        public String message;

        public EventDeleteResponse(String message) {
            this.message = message;
        }
    }

    public static class EventCreateParams {
        public LocalDateTime start;
        public LocalDateTime end;
        public String text;
        public Long userId;
    }

    public static class EventMoveParams {
        public Long id;
        public LocalDateTime start;
        public LocalDateTime end;
    }

    public static class SetColorParams {
        public Long id;
        public String color;
    }
}