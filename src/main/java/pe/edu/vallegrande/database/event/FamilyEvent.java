package pe.edu.vallegrande.database.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FamilyEvent {
    private Integer familyId;
    private String eventType; // "CREATED", "UPDATED", "DELETED"
    private String lastName;
    private String status;
}