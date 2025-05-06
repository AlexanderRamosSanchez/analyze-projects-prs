package pe.edu.vallegrande.database.dto;

import lombok.Data;
import pe.edu.vallegrande.database.model.*;

@Data
public class FamilyDTO {
    private Integer id;
    private String lastName;
    private String direction;
    private String reasibAdmission;
    private Integer numberMembers;
    private Integer numberChildren;
    private String familyType;
    private String socialProblems;
    private String weeklyFrequency;
    private String feedingType;
    private String safeType;
    private String familyDisease;
    private String treatment;
    private String diseaseHistory;
    private String medicalExam;
    private String tenure;
    private String typeOfHousing;
    private String housingMaterial;
    private String housingSecurity;
    private Integer homeEnvironment;
    private Integer bedroomNumber;
    private String habitability;
    private Integer numberRooms;
    private Integer numberOfBedrooms;
    private String habitabilityBuilding;
    private String status;

    private BasicService basicService;
}
