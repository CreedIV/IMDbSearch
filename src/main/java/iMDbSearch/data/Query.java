package iMDbSearch.data;

import java.io.Serializable;

import lombok.Data;

@Data
public class Query  implements Serializable{

	private static final long serialVersionUID = 1L;
  
	private String titleWords;
	private String contributorWords;
}
