package pe.edu.vallegrande.database.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.database.dto.FamilyDTO;
import pe.edu.vallegrande.database.service.FamilyService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/families")
public class FamilyController {

    private final FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    /**
     * Obtiene todas las familias activas
     */
    @GetMapping(value = "/active", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<FamilyDTO> getAllActiveFamilies() {
        return familyService.findAllActive();
    }

    /**
     * Obtiene todas las familias inactivas
     */
    @GetMapping(value = "/inactive", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<FamilyDTO> getAllInactiveFamilies() {
        return familyService.findAllInactive();
    }

    /**
     * Obtiene detalles de una familia por ID
     */
    @GetMapping(value = "/detail/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<FamilyDTO>> getFamilyDetailById(@PathVariable Integer id) {
        return familyService.findDetailById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene una familia por ID
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<FamilyDTO>> getFamilyById(@PathVariable Integer id) {
        return familyService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Crea una nueva familia
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<FamilyDTO>> createFamily(@RequestBody FamilyDTO familyDTO) {
        return familyService.createFamily(familyDTO)
                .map(savedFamilyDTO -> ResponseEntity.status(HttpStatus.CREATED).body(savedFamilyDTO))
                .onErrorResume(this::handleCreationError);
    }

    /**
     * Actualiza una familia existente
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<FamilyDTO>> updateFamily(@PathVariable Integer id, @RequestBody FamilyDTO familyDTO) {
        return familyService.updateFamily(id, familyDTO)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(this::handleUpdateError);
    }

    /**
     * Elimina lógicamente una familia (cambia status a inactivo)
     */
    @PutMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> deleteFamily(@PathVariable Integer id) {
        return familyService.deleteFamily(id)
                .then(Mono.just(ResponseEntity.noContent().build()))
                .onErrorResume(this::handleStatusChangeError);
    }

    /**
     * Activa lógicamente una familia (cambia status a activo)
     */
    @PutMapping(value = "/active/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> activeFamily(@PathVariable Integer id) {
        return familyService.activeFamily(id)
                .then(Mono.just(ResponseEntity.noContent().build()))
                .onErrorResume(this::handleStatusChangeError);
    }

    // Métodos privados para manejo de errores

    private Mono<ResponseEntity<FamilyDTO>> handleCreationError(Throwable e) {
        e.printStackTrace();
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    private Mono<ResponseEntity<FamilyDTO>> handleUpdateError(Throwable e) {
        e.printStackTrace();
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    private Mono<ResponseEntity<Object>> handleStatusChangeError(Throwable e) {
        if (e instanceof IllegalArgumentException) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()));
        }
        e.printStackTrace();
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ha ocurrido un error"));
    }
}