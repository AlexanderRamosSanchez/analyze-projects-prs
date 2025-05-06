package pe.edu.vallegrande.database.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import pe.edu.vallegrande.database.model.Family;
import reactor.core.publisher.Flux;

@Repository
public interface FamilyRepository extends ReactiveCrudRepository<Family, Integer> {
    
    Flux<Family> findAllByStatus(String status);
    
}
