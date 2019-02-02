package iMDbSearch.data;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;


public interface PersonRepository extends CrudRepository<Person, String>{
	
	Set<Person> findByNameContainsIgnoringCase(String partialName);

}
