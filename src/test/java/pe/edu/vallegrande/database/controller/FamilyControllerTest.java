package pe.edu.vallegrande.database.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pe.edu.vallegrande.database.dto.FamilyDTO;
import pe.edu.vallegrande.database.model.BasicService;
import pe.edu.vallegrande.database.service.FamilyService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class FamilyControllerTest {

    private FamilyController familyController;
    private FamilyService familyService;

    @BeforeEach
    public void setUp() {
        familyService = Mockito.mock(FamilyService.class);
        familyController = new FamilyController(familyService);
    }

    @Test
    public void testGetAllActiveFamilies() {
        // Given
        FamilyDTO family1 = createSampleFamilyDTO(1);
        FamilyDTO family2 = createSampleFamilyDTO(2);
        when(familyService.findAllActive()).thenReturn(Flux.just(family1, family2));

        // When & Then
        StepVerifier.create(familyController.getAllActiveFamilies())
                .expectNext(family1)
                .expectNext(family2)
                .verifyComplete();
    }

    @Test
    public void testGetAllInactiveFamilies() {
        // Given
        FamilyDTO family1 = createSampleFamilyDTO(1);
        family1.setStatus("I");
        FamilyDTO family2 = createSampleFamilyDTO(2);
        family2.setStatus("I");
        when(familyService.findAllInactive()).thenReturn(Flux.just(family1, family2));

        // When & Then
        StepVerifier.create(familyController.getAllInactiveFamilies())
                .expectNext(family1)
                .expectNext(family2)
                .verifyComplete();
    }

    @Test
    public void testGetFamilyDetailById_Found() {
        // Given
        Integer familyId = 1;
        FamilyDTO family = createSampleFamilyDTO(familyId);
        when(familyService.findDetailById(familyId)).thenReturn(Mono.just(family));

        // When & Then
        StepVerifier.create(familyController.getFamilyDetailById(familyId))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.OK &&
                                responseEntity.getBody().getId().equals(familyId))
                .verifyComplete();
    }

    @Test
    public void testGetFamilyDetailById_NotFound() {
        // Given
        Integer familyId = 999;
        when(familyService.findDetailById(familyId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(familyController.getFamilyDetailById(familyId))
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }

    @Test
    public void testGetFamilyById_Found() {
        // Given
        Integer familyId = 1;
        FamilyDTO family = createSampleFamilyDTO(familyId);
        when(familyService.findById(familyId)).thenReturn(Mono.just(family));

        // When & Then
        StepVerifier.create(familyController.getFamilyById(familyId))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.OK &&
                                responseEntity.getBody().getId().equals(familyId))
                .verifyComplete();
    }

    @Test
    public void testGetFamilyById_NotFound() {
        // Given
        Integer familyId = 999;
        when(familyService.findById(familyId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(familyController.getFamilyById(familyId))
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }

    @Test
    public void testCreateFamily_Success() {
        // Given
        FamilyDTO familyToCreate = createSampleFamilyDTO(null);
        FamilyDTO createdFamily = createSampleFamilyDTO(1);
        when(familyService.createFamily(any(FamilyDTO.class))).thenReturn(Mono.just(createdFamily));

        // When & Then
        StepVerifier.create(familyController.createFamily(familyToCreate))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.CREATED &&
                                responseEntity.getBody().getId().equals(1))
                .verifyComplete();
    }

    @Test
    public void testCreateFamily_Error() {
        // Given
        FamilyDTO familyToCreate = createSampleFamilyDTO(null);
        when(familyService.createFamily(any(FamilyDTO.class))).thenReturn(Mono.error(new RuntimeException("Error")));

        // When & Then
        StepVerifier.create(familyController.createFamily(familyToCreate))
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == HttpStatus.BAD_REQUEST)
                .verifyComplete();
    }

    @Test
    public void testUpdateFamily_Success() {
        // Given
        Integer familyId = 1;
        FamilyDTO familyToUpdate = createSampleFamilyDTO(familyId);
        FamilyDTO updatedFamily = createSampleFamilyDTO(familyId);
        updatedFamily.setLastName("Updated Last Name");
        when(familyService.updateFamily(eq(familyId), any(FamilyDTO.class))).thenReturn(Mono.just(updatedFamily));

        // When & Then
        StepVerifier.create(familyController.updateFamily(familyId, familyToUpdate))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.OK &&
                                responseEntity.getBody().getId().equals(familyId) &&
                                responseEntity.getBody().getLastName().equals("Updated Last Name"))
                .verifyComplete();
    }

    @Test
    public void testUpdateFamily_NotFound() {
        // Given
        Integer familyId = 999;
        FamilyDTO familyToUpdate = createSampleFamilyDTO(familyId);
        when(familyService.updateFamily(eq(familyId), any(FamilyDTO.class))).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(familyController.updateFamily(familyId, familyToUpdate))
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == HttpStatus.NOT_FOUND)
                .verifyComplete();
    }

    @Test
    public void testUpdateFamily_Error() {
        // Given
        Integer familyId = 1;
        FamilyDTO familyToUpdate = createSampleFamilyDTO(familyId);
        when(familyService.updateFamily(eq(familyId), any(FamilyDTO.class)))
                .thenReturn(Mono.error(new RuntimeException("Error")));

        // When & Then
        StepVerifier.create(familyController.updateFamily(familyId, familyToUpdate))
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR)
                .verifyComplete();
    }

    @Test
    public void testDeleteFamily_Success() {
        // Given
        Integer familyId = 1;
        when(familyService.deleteFamily(familyId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(familyController.deleteFamily(familyId))
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == HttpStatus.NO_CONTENT)
                .verifyComplete();
    }

    @Test
    public void testDeleteFamily_NotFound() {
        // Given
        Integer familyId = 1;
        when(familyService.deleteFamily(familyId))
                .thenReturn(Mono.error(new IllegalArgumentException("Familia no encontrada")));

        // When & Then
        StepVerifier.create(familyController.deleteFamily(familyId))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.NOT_FOUND &&
                                responseEntity.getBody().toString().contains("Familia no encontrada"))
                .verifyComplete();
    }

    @Test
    public void testActiveFamily_Success() {
        // Given
        Integer familyId = 1;
        when(familyService.activeFamily(familyId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(familyController.activeFamily(familyId))
                .expectNextMatches(responseEntity -> responseEntity.getStatusCode() == HttpStatus.NO_CONTENT)
                .verifyComplete();
    }

    @Test
    public void testActiveFamily_NotFound() {
        // Given
        Integer familyId = 1;
        when(familyService.activeFamily(familyId))
                .thenReturn(Mono.error(new IllegalArgumentException("Familia no encontrada")));

        // When & Then
        StepVerifier.create(familyController.activeFamily(familyId))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.NOT_FOUND &&
                                responseEntity.getBody().toString().contains("Familia no encontrada"))
                .verifyComplete();
    }

    // Helper method to create sample Family DTOs
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