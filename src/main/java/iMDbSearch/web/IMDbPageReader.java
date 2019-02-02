package iMDbSearch.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import iMDbSearch.data.Movie;
import iMDbSearch.data.MovieRepository;
import iMDbSearch.data.Person;
import iMDbSearch.data.PersonRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class IMDbPageReader {

	final Integer NUM_MOVIES = 1000;
	
	private final MovieRepository movieRepo;
	
	private final PersonRepository peopleRepo;
	
	@Autowired
	public IMDbPageReader(MovieRepository movieRepo, PersonRepository peopleRepo) {
		this.movieRepo = movieRepo;
		this.peopleRepo = peopleRepo;
	}

	@PostConstruct  // run at spring startup
	@Scheduled(cron = "0 0 0 * * *") // run everyday at midnight
	public void parseIMDbSite() {
    	System.out.println("here");
		Document listingPageDoc = null;
		try {
			String url = "https://www.imdb.com/search/title?groups=top_1000&view=simple&sort=user_rating,desc&start=";
			
			for(int i = 1; i < NUM_MOVIES; i += 50) {
				listingPageDoc = Jsoup.connect(url + i).get(); // get page i
				log.info("movie " + i + " : ");
				
				Elements listingPageMovieItems = listingPageDoc.select(".lister-item-header"); // movie listing elements
				
				for(Element listingPageMovieItem : listingPageMovieItems.select("a")) {
					String movieName = listingPageMovieItem.html();
					log.info("Processing movie : "  + movieName);
					
					String moviePageURL = listingPageMovieItem.attr("abs:href");
					String creditsURL = moviePageURL.substring(0,moviePageURL.lastIndexOf("/")) + "/fullcredits";
					log.info("movie credits page : " + creditsURL);
					
					Document moviePageDoc = Jsoup.connect(creditsURL).get();
					Element credits = moviePageDoc.select("#fullcredits_content").get(0);
										
					
					Movie movie = createMovieFromPage(movieName, credits);
					movie.setIMDblink(moviePageURL);
					
					if(movie.getName().length() < 2) {
						log.warn("movie : " + movie.getName() + " has a potentially incorrect name!");
					}
					if(movie.getDirectors().size() == 0  || movie.getWriters().size() == 0 || movie.getCast().size() == 0) {
						log.warn("movie : " + movie.getName() + " is missing key personel!");
						log.warn("directors : " + movie.getDirectors().toString());
						log.warn("writers : " + movie.getWriters().toString());
						log.warn("cast : " + movie.getCast().toString());
					}
					
					saveMoveAndPeople(movie);			
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
	private void saveMoveAndPeople(Movie movie) {
		List<List<Person>> peopleLists = Arrays.asList(movie.getDirectors(), movie.getWriters(), movie.getCast());
		for(List<Person> people : peopleLists)
			for(Person person : people)
				peopleRepo.save(person);
		
		movieRepo.save(movie);
	}

	private Movie createMovieFromPage(String movieName, Element credits) {
		Movie movie = new Movie(movieName);
		
		int i = 0;
		for(Element table : credits.select("table")) {
			if(++i > 3) // i'm dependent on table order, director, writers, cast. dont process more tables.
				return movie;
			
			// this is broken.... I should depend on table order 1st director, writer, cast
			//Element creditItem = (table.siblingElements()).select(".dataHeaderWithBorder").get(table.siblingIndex()-1); // credit item type is contained in sibling of table...
			//String creditType = cleanCreditType(creditItem.html()); 
			
			List<Person> people = new ArrayList<Person>();

			switch(i) {  // store name array according to data it holds;
			case 1:	
				fillDataFromTable(table, people);
				log.info("setting directors : " + people);
				movie.setDirectors(people);
				break;
			case 2:
				fillDataFromTable(table, people);
				log.info("setting writers : " + people);
				movie.setWriters(people);
				break;
			case 3:
				fillCastFromTable(table, people);
				log.info("setting cast : " + people);
				movie.setCast(people);
				break;
			}
		}
		return movie;
	}

	private void fillCastFromTable(Element table, List<Person> people) {
		for(Element tableRow : table.select("tr")) {
			//System.out.println("got table row : " + tableRow);
			for(Element tableData : tableRow.select(".primary_photo")) {
				String name = tableData.select("img").attr("title");
				log.debug("credit to : " + name);
				people.add(new Person(name));
			}
		}	
	}

	private void fillDataFromTable(Element table, List<Person> people) {
		for(Element tableRow : table.select("tr")) {
			for(Element tableData : tableRow.select(".name")) {
				String name = tableData.select("a").html();
				log.debug("credit to : " + name);
				people.add(new Person(name));
			}
		}		
	}

	private String cleanCreditType(String creditType) {
		creditType = creditType.replaceAll("&nbsp;", "");
		if(creditType.startsWith("Cast ")) { // remove stuff like " <span>(in credits order)</span> <span> verified as complete </span>" 
			creditType = "Cast";   // assumes Cast is the only items starting with cast...
		}
		if(creditType.startsWith("Writing Credits ")) { // remove this type of "stuff" : <span>(<a href="https://help.imdb.com/article/imdb/general-information/imdb-partners/G8TZTG4LR6ZV4LXZ?ref_=cons_tt_writer_wga#GVTJ2XVZFGX5YDW9">WGA</a>)</span> 
			creditType = "Writing Credits";
		}
		return creditType;
	}
}
