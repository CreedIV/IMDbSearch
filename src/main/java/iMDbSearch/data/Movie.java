package iMDbSearch.data;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.AccessLevel;

@Data 
@Entity 
@RequiredArgsConstructor
@NoArgsConstructor(access=AccessLevel.PRIVATE, force=true)
public class Movie {
	
	@Id
	private final String name; 
	
	private String iMDblink;
	
	@ManyToMany
	private List<Person> directors;
	
	@ManyToMany
	private List<Person> writers;
	
	@ManyToMany
	private List<Person> cast;



	
}
