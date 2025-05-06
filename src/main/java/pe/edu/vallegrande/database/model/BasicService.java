package pe.edu.vallegrande.database.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
@Table("services_and_environment")
public class BasicService {
    @Id
    private Integer serviceId;
    private String waterService;
    private String servDrain;
    private String servLight;
    private String servCable;
    private String servGas;
    private String area;
    private String referenceLocation;
    private String residue;
    private String publicLighting;
    private String security;
    private String material;
    private String feeding;
    private String economic;
    private String spiritual;
    private String socialCompany;
    private String guideTip;
}