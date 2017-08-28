/**
 * 
 */
package edu.illinois.cs.cogcomp.acl10.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.cs.cogcomp.utils.IOManager;

/**
 * @author dxquang Feb 7, 2010
 */
public class PreparePantelData {

	private static Map<String, String> mapPantelCategories = new HashMap<String, String>();
	static {
		mapPantelCategories.put("classical_pianists.txt", "pianist");
		mapPantelCategories.put("composers.txt", "composer");
		mapPantelCategories.put("formula_one_drivers.txt", "driver");
		mapPantelCategories.put("astronomers.txt", "astronomer");
		mapPantelCategories.put("english_poets.txt", "poet");
		mapPantelCategories.put("greek_gods.txt", "god");
		mapPantelCategories.put("male_tennis_players.txt", "tennis player");
		mapPantelCategories.put("north_american_mountain_ranges.txt", "mountain range");
		mapPantelCategories.put("stars.txt", "star");
		mapPantelCategories.put("superheros.txt", "superhero");
		mapPantelCategories.put("french_artists.txt", "artist");
		mapPantelCategories.put("greek_islands.txt", "island");
		mapPantelCategories.put("cocktails.txt", "cocktail");
		mapPantelCategories.put("texas_counties.txt", "county");
		mapPantelCategories.put("countries.txt", "country");
		mapPantelCategories.put("russian_authors.txt", "author");
		mapPantelCategories.put("us_army_generals.txt", "general");
		mapPantelCategories.put("electronic_companies.txt", "company");
		mapPantelCategories.put("charitable_foundations.txt", "foundation");
		mapPantelCategories.put("roman_emperors.txt", "emperor");
		mapPantelCategories.put("us_internet_companies.txt", "company");
		mapPantelCategories.put("archbishops_of_canterbury.txt", "archbishop");
		mapPantelCategories.put("elements.txt", "element");
		mapPantelCategories.put("australian_cities.txt", "city");
		mapPantelCategories.put("japanese_martial_arts.txt", "martial art");
		mapPantelCategories.put("cognitive_scientists.txt", "scientist");
		mapPantelCategories.put("canadian_universities.txt", "university");
		mapPantelCategories.put("biology_disciplines.txt", "discipline");
		mapPantelCategories.put("bottled_water_brands.txt", "water");
		mapPantelCategories.put("presidents_of_argentina.txt", "president");
		mapPantelCategories.put("best_actress_academy_award_winners.txt", "academy award");
		mapPantelCategories.put("california_counties.txt", "county");
		mapPantelCategories.put("english_cities.txt", "city");
		mapPantelCategories.put("spanish_provinces.txt", "province");
		mapPantelCategories.put("japanese_prefectures.txt", "prefecture");
		mapPantelCategories.put("australian_airlines.txt", "airline");
		mapPantelCategories.put("english_premier_football_clubs.txt", "football");
		mapPantelCategories.put("first_ladies.txt", "first lady");
		mapPantelCategories.put("irish_theatres.txt", "theatre");
		mapPantelCategories.put("rivers_in_england.txt", "river");
		mapPantelCategories.put("nhl_hockey_teams.txt", "hockey");
		mapPantelCategories.put("australian_prime_ministers.txt", "prime minister");
		mapPantelCategories.put("maryland_counties.txt", "county");
		mapPantelCategories.put("new_zealand_songwriters.txt", "songwriter");
		mapPantelCategories.put("italian_regions.txt", "region");
		mapPantelCategories.put("us_federal_departments.txt", "department");
		mapPantelCategories.put("boxing_weight_classes.txt", "boxing");
		mapPantelCategories.put("canadian_stadiums.txt", "stadium");
		mapPantelCategories.put("australian_a-league_football_teams.txt", "football");
	}
	
	
	public static void prepareData(String pantelFolder, String outputFile) {

		String[] files = IOManager.listDirectory(pantelFolder);
		
		ArrayList<String> arrConcepts = new ArrayList<String>();

		for (String file : files) {

			String category = mapPantelCategories.get(file);
			
			String path = pantelFolder + "/" + file;
			System.out.println(path);

			ArrayList<String> lines = IOManager.readLines(path);

			for (String line : lines) {
				
				if (line.length() == 0 || line.startsWith("#"))
						continue;
			
				String[] parts = line.split("\\t+");
				arrConcepts.add(parts[0] + "\t" + category);
				
			}

		}
		
		IOManager.writeLinesAddingReturn(arrConcepts, outputFile);
	}
}
