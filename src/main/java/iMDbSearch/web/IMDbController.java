package iMDbSearch.web;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import iMDbSearch.data.Movie;
import iMDbSearch.data.MovieRepository;
import iMDbSearch.data.Person;
import iMDbSearch.data.PersonRepository;
import iMDbSearch.data.Query;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/")
@Slf4j
public class IMDbController {
		
	
	private final MovieRepository movieRepo;
	
	private final PersonRepository personRepo;
	
	@Autowired
	public IMDbController(MovieRepository movieRepo, PersonRepository personRepo) {
		this.movieRepo = movieRepo;
		this.personRepo = personRepo;
	}
	
	@GetMapping({"/home", "/"})
	public String home(Model model){
		model.addAttribute("query", new Query());
		return "home";
	}
	
	@PostMapping("/search")
	public String search(Query query, Model model) {
		log.info("searching for title words : " + query.getTitleWords());
		log.info("searching for contributor words : " + query.getContributorWords());

		Set<Movie> results = new HashSet<Movie>();
		
		movieRepo.findAll().forEach(results::add);
		
		for(String word : query.getTitleWords().split(" ")) {
			if(!word.trim().equals("")) 
				results.retainAll(movieRepo.findByNameContainsIgnoringCase(word.trim()));
		}
		
		// we use this because a search like "speilberg liam" can return many people for liam, and we only want the movie to have at least one of those people, not all of them
		Map<String, Set<Person>> keywordMovieMap = new HashMap<String, Set<Person>>(); 
		
		for(String word : query.getContributorWords().split(" ")) {
			if(!word.trim().equals("")) {
				Set<Person> peopleResults = personRepo.findByNameContainsIgnoringCase(word.trim());
				if(peopleResults.size() > 0) {
					keywordMovieMap.put(word, peopleResults);
					log.debug("found people for word : " + word);
				}else
					log.debug("failed to find people for word : " + word);
				log.debug(peopleResults.toString());
			}
		}
		for(String keyword : keywordMovieMap.keySet()) {
			Set<Movie> resultsForPersonKeyword = new HashSet<Movie>();
			for(Person person : keywordMovieMap.get(keyword)) {
				resultsForPersonKeyword.addAll(movieRepo.findByCastContains(person));
				resultsForPersonKeyword.addAll(movieRepo.findByDirectorsContains(person));
				resultsForPersonKeyword.addAll(movieRepo.findByWritersContains(person));
			}
			log.debug("found movies for person keyword " + keyword + " : " + resultsForPersonKeyword.toString());
			results.retainAll(resultsForPersonKeyword); // retain any movie which has a person found from this keyword....
		}
		
		model.addAttribute("movies", results);
		return "results";
	}
}
