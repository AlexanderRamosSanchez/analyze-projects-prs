package pe.edu.vallegrande.database.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.database.event.FamilyEvent;
import pe.edu.vallegrande.database.model.Family;

@Service
public class FamilyEventService {

    private static final String TOPIC_NAME = "family-events";

    @Autowired
    private KafkaTemplate<String, FamilyEvent> kafkaTemplate;

    public void publishFamilyEvent(Family family, String eventType) {
        FamilyEvent event = new FamilyEvent(
                family.getId(),
                eventType,
                family.getLastName(),
                family.getStatus()
        );

        kafkaTemplate.send(TOPIC_NAME, String.valueOf(family.getId()), event);
    }
}
