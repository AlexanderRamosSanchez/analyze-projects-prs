package pe.edu.vallegrande.database.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import pe.edu.vallegrande.database.model.BasicService;

@Repository
public interface BasicServiceRepository extends ReactiveCrudRepository<BasicService, Integer> {
     
}
