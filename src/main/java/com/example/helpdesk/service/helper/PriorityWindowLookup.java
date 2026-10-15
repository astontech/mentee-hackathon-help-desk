package com.example.helpdesk.service.helper;

import com.example.helpdesk.domain.Priority;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

@Component
public class PriorityWindowLookup {

    // An EnumMap is faster than a HashMap because it uses a highly efficient internal array
    private final Map<Priority, Duration> PRIORITY_WINDOW_VALUES = new EnumMap<>(Priority.class);



    public PriorityWindowLookup(
            @Value("${escalation.windows.LOW}") Duration lowPriorityWindow,
            @Value("${escalation.windows.NORMAL}") Duration normalPriorityWindow,
            @Value("${escalation.windows.HIGH}") Duration highPriorityWindow,
            @Value("${escalation.windows.URGENT}") Duration urgentPriorityWindow
    ) {
        PRIORITY_WINDOW_VALUES.put(Priority.LOW, lowPriorityWindow);
        PRIORITY_WINDOW_VALUES.put(Priority.NORMAL, normalPriorityWindow);
        PRIORITY_WINDOW_VALUES.put(Priority.HIGH, highPriorityWindow);
        PRIORITY_WINDOW_VALUES.put(Priority.URGENT, urgentPriorityWindow);

    }

    public Duration getWindowValurForStatus(Priority priority) {
        return PRIORITY_WINDOW_VALUES.getOrDefault(priority, null);
    }
}