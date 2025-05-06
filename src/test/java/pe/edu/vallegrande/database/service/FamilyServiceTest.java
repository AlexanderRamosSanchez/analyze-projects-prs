package pe.edu.vallegrande.database.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pe.edu.vallegrande.database.dto.FamilyDTO;
import pe.edu.vallegrande.database.model.BasicService;
import pe.edu.vallegrande.database.model.Family;
import pe.edu.vallegrande.database.repository.BasicServiceRepository;
import pe.edu.vallegrande.database.repository.FamilyRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FamilyServiceTest {

    private FamilyService familyService;
    private FamilyRepository familyRepository;
    private BasicServiceRepository basicServiceRepository;
    private FamilyEventService familyEventService;

    @BeforeEach
    public void setUp() {
        familyRepository = Mockito.mock(FamilyRepository.class);
        basicServiceRepository = Mockito.mock(BasicServiceRepository.class);
        familyEventService = Mockito.mock(FamilyEventService.class);

        familyService = new FamilyService(basicServiceRepository, familyRepository, familyEventService);
    }

    @Test
    public void testFindAllActive() {
        // Given
        Family family1 = createSampleFamily(1, "A");
        Family family2 = createSampleFamily(2, "A");

        BasicService service1 = createSampleBasicService(1);
        BasicService service2 = createSampleBasicService(2);

        when(familyRepository.findAllByStatus("A")).thenReturn(Flux.just(family1, family2));
        when(basicServiceRepository.findById(1)).thenReturn(Mono.just(service1));
        when(basicServiceRepository.findById(2)).thenReturn(Mono.just(service2));

        // When & Then
        StepVerifier.create(familyService.findAllActive())
                .expectNextMatches(dto -> dto.getId().equals(1) && dto.getStatus().equals("A"))
                .expectNextMatches(dto -> dto.getId().equals(2) && dto.getStatus().equals("A"))
                .verifyComplete();
    }

    @Test
    public void testFindAllInactive() {
        // Given
        Family family1 = createSampleFamily(1, "I");
        Family family2 = createSampleFamily(2, "I");

        BasicService service1 = createSampleBasicService(1);
        BasicService service2 = createSampleBasicService(2);

        when(familyRepository.findAllByStatus("I")).thenReturn(Flux.just(family1, family2));
        when(basicServiceRepository.findById(1)).thenReturn(Mono.just(service1));
        when(basicServiceRepository.findById(2)).thenReturn(Mono.just(service2));

        // When & Then
        StepVerifier.create(familyService.findAllInactive())
                .expectNextMatches(dto -> dto.getId().equals(1) && dto.getStatus().equals("I"))
                .expectNextMatches(dto -> dto.getId().equals(2) && dto.getStatus().equals("I"))
                .verifyComplete();
    }

    @Test
    public void testFindById_Exists() {
        // Given
        Integer familyId = 1;
        Family family = createSampleFamily(familyId, "A");
        BasicService service = createSampleBasicService(familyId);

        when(familyRepository.findById(familyId)).thenReturn(Mono.just(family));
        when(basicServiceRepository.findById(familyId)).thenReturn(Mono.just(service));

        // When & Then
        StepVerifier.create(familyService.findById(familyId))
                .expectNextMatches(dto ->
                        dto.getId().equals(familyId) &&
                                dto.getBasicService() != null &&
                                dto.getBasicService().getServiceId().equals(familyId))
                .verifyComplete();
    }

    @Test
    public void testFindById_NotExists() {
        // Given
        Integer familyId = 999;
        when(familyRepository.findById(familyId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(familyService.findById(familyId))
                .expectComplete()
                .verify();
    }

    @Test
    public void testCreateFamily_WithBasicService() {
        // Given
        FamilyDTO familyDTO = createSampleFamilyDTO(null);
        BasicService basicService = createSampleBasicService(null);
        BasicService savedBasicService = createSampleBasicService(1);
        Family savedFamily = createSampleFamily(1, "A");

        familyDTO.setBasicService(basicService);

        when(basicServiceRepository.save(any(BasicService.class))).thenReturn(Mono.just(savedBasicService));
        when(familyRepository.save(any(Family.class))).thenReturn(Mono.just(savedFamily));

        // Mock void method to do nothing
        Mockito.doNothing().when(familyEventService).publishFamilyEvent(any(Family.class), eq("CREATED"));

        // When & Then
        StepVerifier.create(familyService.createFamily(familyDTO))
                .expectNextMatches(dto ->
                        dto.getId().equals(1) &&
                                dto.getStatus().equals("A") &&
                                dto.getBasicService() != null)
                .verifyComplete();

        verify(familyEventService).publishFamilyEvent(any(Family.class), eq("CREATED"));
    }

    @Test
    public void testUpdateFamily_Success() {
        // Given
        Integer familyId = 1;
        FamilyDTO familyDTO = createSampleFamilyDTO(familyId);
        familyDTO.setLastName("Updated Last Name");

        Family existingFamily = createSampleFamily(familyId, "A");
        Family updatedFamily = createSampleFamily(familyId, "A");
        updatedFamily.setLastName("Updated Last Name");

        BasicService existingService = createSampleBasicService(familyId);
        BasicService updatedService = createSampleBasicService(familyId);

        when(familyRepository.findById(familyId)).thenReturn(Mono.just(existingFamily));
        when(familyRepository.save(any(Family.class))).thenReturn(Mono.just(updatedFamily));
        when(basicServiceRepository.findById(familyId)).thenReturn(Mono.just(existingService));
        when(basicServiceRepository.save(any(BasicService.class))).thenReturn(Mono.just(updatedService));

        // Mock void method to do nothing
        Mockito.doNothing().when(familyEventService).publishFamilyEvent(any(Family.class), eq("UPDATED"));

        // When & Then
        StepVerifier.create(familyService.updateFamily(familyId, familyDTO))
                .expectNextMatches(dto ->
                        dto.getId().equals(familyId) &&
                                dto.getLastName().equals("Updated Last Name"))
                .verifyComplete();

        verify(familyEventService).publishFamilyEvent(any(Family.class), eq("UPDATED"));
    }

    @Test
    public void testDeleteFamily_Success() {
        // Given
        Integer familyId = 1;
        Family existingFamily = createSampleFamily(familyId, "A");
        Family inactiveFamily = createSampleFamily(familyId, "I");

        when(familyRepository.findById(familyId)).thenReturn(Mono.just(existingFamily));
        when(familyRepository.save(any(Family.class))).thenReturn(Mono.just(inactiveFamily));

        // Mock void method to do nothing
        Mockito.doNothing().when(familyEventService).publishFamilyEvent(any(Family.class), eq("DELETED"));

        // When & Then
        StepVerifier.create(familyService.deleteFamily(familyId))
                .verifyComplete();

        verify(familyEventService).publishFamilyEvent(any(Family.class), eq("DELETED"));
    }

    @Test
    public void testDeleteFamily_NotFound() {
        // Given
        Integer familyId = 999;
        when(familyRepository.findById(familyId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(familyService.deleteFamily(familyId))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void testActiveFamily_Success() {
        // Given
        Integer familyId = 1;
        Family existingFamily = createSampleFamily(familyId, "I");
        Family activeFamily = createSampleFamily(familyId, "A");

        when(familyRepository.findById(familyId)).thenReturn(Mono.just(existingFamily));
        when(familyRepository.save(any(Family.class))).thenReturn(Mono.just(activeFamily));

        // Mock void method to do nothing
        Mockito.doNothing().when(familyEventService).publishFamilyEvent(any(Family.class), eq("UPDATED"));

        // When & Then
        StepVerifier.create(familyService.activeFamily(familyId))
                .verifyComplete();

        verify(familyEventService).publishFamilyEvent(any(Family.class), eq("UPDATED"));
    }

    @Test
    public void testActiveFamily_NotFound() {
        // Given
        Integer familyId = 999;
        when(familyRepository.findById(familyId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(familyService.activeFamily(familyId))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    // Helper method to create sample Family entity
    private Family createSampleFamily(Integer id, String status) {
        Family family = new Family();
        family.setId(id);
        family.setLastName("Test Family");
        family.setDirection("123 Test Street");
        family.setReasibAdmission("Testing");
        family.setNumberMembers(4);
        family.setNumberChildren(2);
        family.setFamilyType("Nuclear");
        family.setStatus(status);
        family.setServiceId(id); // Link to BasicService
        return family;
    }

    // Helper method to create sample BasicService entity
    private BasicService createSampleBasicService(Integer id) {
        return BasicService.builder()
                .serviceId(id)
                .waterService("Yes")
                .servDrain("Yes")
                .servLight("Yes")
                .build();
    }

    // Helper method to create sample FamilyDTO
    private FamilyDTO createSampleFamilyDTO(Integer id) {
        FamilyDTO familyDTO = new FamilyDTO();
        if (id != null) {
            familyDTO.setId(id);
        }
        familyDTO.setLastName("Test Family");
        familyDTO.setDirection("123 Test Street");
        familyDTO.setReasibAdmission("Testing");
        familyDTO.setNumberMembers(4);
        familyDTO.setNumberChildren(2);
        familyDTO.setFamilyType("Nuclear");
        familyDTO.setStatus("A");

        // Sample basic service
        BasicService basicService = BasicService.builder()
                .serviceId(id)
                .waterService("Yes")
                .servDrain("Yes")
                .servLight("Yes")
                .build();

        familyDTO.setBasicService(basicService);

        return familyDTO;
    }
}