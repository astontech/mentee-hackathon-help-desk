package com.example.helpdesk;

import com.example.helpdesk.domain.Priority;
import com.example.helpdesk.service.helper.PriorityWindowLookup;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class PriorityWindowLookupTests {

    @Test
    void returnsTheWindowItWasBuiltWithForEachPriority() {
        //arrange
        Duration low = Duration.ofDays(10);
        Duration normal = Duration.ofDays(4);
        Duration high = Duration.ofHours(4);
        Duration urgent = Duration.ofMinutes(30);

        PriorityWindowLookup windowLookup = new PriorityWindowLookup(
                low,
                normal,
                high,
                urgent
        );

        //act
        Duration lowWindow = windowLookup.getWindowValueForPriority(Priority.LOW);
        Duration normalWindow = windowLookup.getWindowValueForPriority(Priority.NORMAL);
        Duration highWindow = windowLookup.getWindowValueForPriority(Priority.HIGH);
        Duration urgentWindow = windowLookup.getWindowValueForPriority(Priority.URGENT);

        //assert
        assertThat(lowWindow).isEqualTo(low);
        assertThat(normalWindow).isEqualTo(normal);
        assertThat(highWindow).isEqualTo(high);
        assertThat(urgentWindow).isEqualTo(urgent);
    }
}
