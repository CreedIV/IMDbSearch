package iMDbSearch.data;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;

public interface MovieRepository extends CrudRepository<Movie, String>{
	
	
	Set<Movie> findByCastContains(Person partialCast);

	Set<Movie> findByDirectorsContains(Person person);
	
	Set<Movie> findByWritersContains(Person person);

	Set<Movie> findByNameContainsIgnoringCase(String word);

}
