package pe.edu.vallegrande.database.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.database.dto.FamilyDTO;
import pe.edu.vallegrande.database.model.*;
import pe.edu.vallegrande.database.repository.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FamilyService {

    private final BasicServiceRepository basicServiceRepository;
    private final FamilyRepository familyRepository;
    private final FamilyEventService familyEventService;

    @Autowired
    public FamilyService(BasicServiceRepository basicServiceRepository,
                         FamilyRepository familyRepository,
                         FamilyEventService familyEventService) {
        this.basicServiceRepository = basicServiceRepository;
        this.familyRepository = familyRepository;
        this.familyEventService = familyEventService;
    }

    /**
     * Mapea una entidad Family a un FamilyDTO incluyendo sus servicios básicos
     */
    public Mono<FamilyDTO> mapToFamilyDTO(Family family) {
        FamilyDTO dto = mapFamilyToDTO(family);

        if (family.getServiceId() != null) {
            return basicServiceRepository.findById(family.getServiceId())
                    .map(basicService -> {
                        dto.setBasicService(basicService);
                        return dto;
                    })
                    .defaultIfEmpty(dto);
        }

        return Mono.just(dto);
    }

    /**
     * Obtiene listado de familias activas
     */
    public Flux<FamilyDTO> findAllActive() {
        return familyRepository.findAllByStatus("A")
                .sort((f1, f2) -> f1.getId().compareTo(f2.getId()))
                .flatMap(this::mapToFamilyDTO);
    }

    /**
     * Obtiene listado de familias inactivas
     */
    public Flux<FamilyDTO> findAllInactive() {
        return familyRepository.findAllByStatus("I")
                .sort((f1, f2) -> f1.getId().compareTo(f2.getId()))
                .flatMap(this::mapToFamilyDTO);
    }

    /**
     * Obtiene una familia por ID
     */
    public Mono<FamilyDTO> findById(Integer id) {
        return familyRepository.findById(id)
                .flatMap(this::mapToFamilyDTO);
    }

    /**
     * Crea una nueva familia con sus servicios asociados
     */
    public Mono<FamilyDTO> createFamily(FamilyDTO familyDTO) {
        Mono<BasicService> serviceOperation = createOrGetBasicService(familyDTO);

        return serviceOperation.flatMap(savedBasicService -> {
                    Family family = mapDTOToFamily(familyDTO);
                    family.setStatus("A"); // Active by default
                    family.setServiceId(savedBasicService.getServiceId());

                    return familyRepository.save(family)
                            .doOnSuccess(savedFamily -> familyEventService.publishFamilyEvent(savedFamily, "CREATED"))
                            .flatMap(this::mapToFamilyDTO);
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.error(new RuntimeException("Error durante la creación de la familia: " + e.getMessage()));
                });
    }

    /**
     * Actualiza una familia existente y sus servicios
     */
    public Mono<FamilyDTO> updateFamily(Integer id, FamilyDTO familyDTO) {
        return familyRepository.findById(id)
                .flatMap(existingFamily -> {
                    updateFamilyFromDTO(existingFamily, familyDTO);
                    Mono<Family> savedFamilyMono = familyRepository.save(existingFamily)
                            .doOnSuccess(savedFamily -> familyEventService.publishFamilyEvent(savedFamily, "UPDATED"));

                    return updateBasicServiceIfExists(existingFamily, familyDTO)
                            .defaultIfEmpty(existingFamily)
                            .then(savedFamilyMono);
                })
                .flatMap(this::mapToFamilyDTO)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.error(new RuntimeException("Error durante la actualización de la familia: " + e.getMessage()));
                });
    }

    /**
     * Desactiva lógicamente una familia
     */
    public Mono<Void> deleteFamily(Integer id) {
        return familyRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Familia no encontrada con ID: " + id)))
                .flatMap(family -> {
                    family.setStatus("I"); // Inactive
                    return familyRepository.save(family)
                            .doOnSuccess(savedFamily -> familyEventService.publishFamilyEvent(savedFamily, "DELETED"))
                            .then();
                });
    }

    /**
     * Activa lógicamente una familia
     */
    public Mono<Void> activeFamily(Integer id) {
        return familyRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Familia no encontrada con ID: " + id)))
                .flatMap(family -> {
                    family.setStatus("A"); // Active
                    return familyRepository.save(family)
                            .doOnSuccess(savedFamily -> familyEventService.publishFamilyEvent(savedFamily, "UPDATED"))
                            .then();
                });
    }

    /**
     * Obtiene detalles de una familia (idéntico a findById en este caso)
     */
    public Mono<FamilyDTO> findDetailById(Integer id) {
        return findById(id);
    }

    // Métodos privados auxiliares para mejorar la legibilidad

    private FamilyDTO mapFamilyToDTO(Family family) {
        FamilyDTO dto = new FamilyDTO();
        dto.setId(family.getId());
        dto.setLastName(family.getLastName());
        dto.setDirection(family.getDirection());
        dto.setReasibAdmission(family.getReasibAdmission());
        dto.setNumberMembers(family.getNumberMembers());
        dto.setNumberChildren(family.getNumberChildren());
        dto.setFamilyType(family.getFamilyType());
        dto.setSocialProblems(family.getSocialProblems());
        dto.setWeeklyFrequency(family.getWeeklyFrequency());
        dto.setFeedingType(family.getFeedingType());
        dto.setSafeType(family.getSafeType());
        dto.setFamilyDisease(family.getFamilyDisease());
        dto.setTreatment(family.getTreatment());
        dto.setDiseaseHistory(family.getDiseaseHistory());
        dto.setMedicalExam(family.getMedicalExam());
        dto.setTenure(family.getTenure());
        dto.setTypeOfHousing(family.getTypeOfHousing());
        dto.setHousingMaterial(family.getHousingMaterial());
        dto.setHousingSecurity(family.getHousingSecurity());
        dto.setHomeEnvironment(family.getHomeEnvironment());
        dto.setBedroomNumber(family.getBedroomNumber());
        dto.setHabitability(family.getHabitability());
        dto.setNumberRooms(family.getNumberRooms());
        dto.setNumberOfBedrooms(family.getNumberOfBedrooms());
        dto.setHabitabilityBuilding(family.getHabitabilityBuilding());
        dto.setStatus(family.getStatus());
        return dto;
    }

    private Family mapDTOToFamily(FamilyDTO dto) {
        Family family = new Family();
        family.setLastName(dto.getLastName());
        family.setDirection(dto.getDirection());
        family.setReasibAdmission(dto.getReasibAdmission());
        family.setNumberMembers(dto.getNumberMembers());
        family.setNumberChildren(dto.getNumberChildren());
        family.setFamilyType(dto.getFamilyType());
        family.setSocialProblems(dto.getSocialProblems());
        family.setWeeklyFrequency(dto.getWeeklyFrequency());
        family.setFeedingType(dto.getFeedingType());
        family.setSafeType(dto.getSafeType());
        family.setFamilyDisease(dto.getFamilyDisease());
        family.setTreatment(dto.getTreatment());
        family.setDiseaseHistory(dto.getDiseaseHistory());
        family.setMedicalExam(dto.getMedicalExam());
        family.setTenure(dto.getTenure());
        family.setTypeOfHousing(dto.getTypeOfHousing());
        family.setHousingMaterial(dto.getHousingMaterial());
        family.setHousingSecurity(dto.getHousingSecurity());
        family.setHomeEnvironment(dto.getHomeEnvironment());
        family.setBedroomNumber(dto.getBedroomNumber());
        family.setHabitability(dto.getHabitability());
        family.setNumberRooms(dto.getNumberRooms());
        family.setNumberOfBedrooms(dto.getNumberOfBedrooms());
        family.setHabitabilityBuilding(dto.getHabitabilityBuilding());
        return family;
    }

    private void updateFamilyFromDTO(Family family, FamilyDTO dto) {
        family.setLastName(dto.getLastName());
        family.setDirection(dto.getDirection());
        family.setReasibAdmission(dto.getReasibAdmission());
        family.setNumberMembers(dto.getNumberMembers());
        family.setNumberChildren(dto.getNumberChildren());
        family.setFamilyType(dto.getFamilyType());
        family.setSocialProblems(dto.getSocialProblems());
        family.setWeeklyFrequency(dto.getWeeklyFrequency());
        family.setFeedingType(dto.getFeedingType());
        family.setSafeType(dto.getSafeType());
        family.setFamilyDisease(dto.getFamilyDisease());
        family.setTreatment(dto.getTreatment());
        family.setDiseaseHistory(dto.getDiseaseHistory());
        family.setMedicalExam(dto.getMedicalExam());
        family.setTenure(dto.getTenure());
        family.setTypeOfHousing(dto.getTypeOfHousing());
        family.setHousingMaterial(dto.getHousingMaterial());
        family.setHousingSecurity(dto.getHousingSecurity());
        family.setHomeEnvironment(dto.getHomeEnvironment());
        family.setBedroomNumber(dto.getBedroomNumber());
        family.setHabitability(dto.getHabitability());
        family.setNumberRooms(dto.getNumberRooms());
        family.setNumberOfBedrooms(dto.getNumberOfBedrooms());
        family.setHabitabilityBuilding(dto.getHabitabilityBuilding());
        // Status no se actualiza para mantener consistencia
    }

    private Mono<BasicService> createOrGetBasicService(FamilyDTO familyDTO) {
        if (familyDTO.getBasicService() != null) {
            return basicServiceRepository.save(familyDTO.getBasicService());
        } else {
            return Mono.just(BasicService.builder().build());
        }
    }

    private Mono<Family> updateBasicServiceIfExists(Family family, FamilyDTO familyDTO) {
        if (family.getServiceId() != null && familyDTO.getBasicService() != null) {
            return basicServiceRepository.findById(family.getServiceId())
                    .flatMap(existingService -> {
                        updateBasicServiceFromDTO(existingService, familyDTO.getBasicService());
                        return basicServiceRepository.save(existingService)
                                .thenReturn(family);
                    });
        }
        return Mono.empty();
    }

    private void updateBasicServiceFromDTO(BasicService service, BasicService dto) {
        service.setWaterService(dto.getWaterService());
        service.setServDrain(dto.getServDrain());
        service.setServLight(dto.getServLight());
        service.setServCable(dto.getServCable());
        service.setServGas(dto.getServGas());
        service.setArea(dto.getArea());
        service.setReferenceLocation(dto.getReferenceLocation());
        service.setResidue(dto.getResidue());
        service.setPublicLighting(dto.getPublicLighting());
        service.setSecurity(dto.getSecurity());
        service.setMaterial(dto.getMaterial());
        service.setFeeding(dto.getFeeding());
        service.setEconomic(dto.getEconomic());
        service.setSpiritual(dto.getSpiritual());
        service.setSocialCompany(dto.getSocialCompany());
        service.setGuideTip(dto.getGuideTip());
    }
}