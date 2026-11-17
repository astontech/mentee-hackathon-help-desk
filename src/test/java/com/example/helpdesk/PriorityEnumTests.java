package com.example.helpdesk;

import com.example.helpdesk.domain.Priority;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PriorityEnumTests {
    @Test
    void returnsNextPriority(){
        //arrange
        Priority lowToNormal = Priority.LOW;
        Priority normalToHigh = Priority.NORMAL;
        Priority highToUrgent = Priority.HIGH;
        Priority urgentToUrgent = Priority.URGENT;

        //act
        lowToNormal = lowToNormal.next();
        normalToHigh = normalToHigh.next();
        highToUrgent = highToUrgent.next();
        urgentToUrgent = urgentToUrgent.next();

        //assert
        assertThat(lowToNormal).isEqualTo(Priority.NORMAL);
        assertThat(normalToHigh).isEqualTo(Priority.HIGH);
        assertThat(highToUrgent).isEqualTo(Priority.URGENT);
        assertThat(urgentToUrgent).isEqualTo(Priority.URGENT);
    }
}
