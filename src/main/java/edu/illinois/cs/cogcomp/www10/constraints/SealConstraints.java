/**
 * 
 */
package edu.illinois.cs.cogcomp.www10.constraints;

import edu.illinois.cs.cogcomp.cikm09.identification.RelationIdentification;
import edu.illinois.cs.cogcomp.cikm09.learning.MainRelationIdentification;

/**
 * @author dxquang Oct 2, 2009
 */
public class SealConstraints {

	MainRelationIdentification mri = null;

	String[] siblingConceptCorrect = new String[] { "Nissan Pathfinder",
			"carmodel", "pontiac bonneville", "mazda mpv", "Natalie Portman",
			"actor", "ethel merman", "alyson hannigan", "Peony", "flower",
			"magnolia", "anemone", "Susquehanna River", "river",
			"sabine river", "yazoo river", "Arsenal", "soccerclub",
			"charlton athletic", "ac milan", "Mali", "country", "niger",
			"ghana", "Tekken 3", "videogame", "grandia", "wwf warzone",
			"Hamburger SV", "soccerclub", "grazer ak", "olympiacos piraeus",
			"Stade du Pays de Charleroi", "stadium", "mattoangin stadium",
			"ltu-arena", "Scotia Plaza", "skyscraper", "first canadian place",
			"wachovia financial center", "Olympic National Park",
			"nationalpark", "redwood national park",
			"everglades national park", "SV Mattersburg", "soccerclub",
			"fc wacker tirol", "scr altach", "Algol", "skybody",
			"deneb kaitos", "scheat", "Hydrochlorothiazide", "drug",
			"glimepiride", "hydroxyzine", "Battle of shiloh reenactment",
			"worldwarbattle", "battle of hurtgen forest",
			"battle of surigao strait", "Sirius", "skybody", "phecda",
			"antares", "Boeing 767", "aircraftmodel", "airbus a300",
			"boeing 757", "Ed Harris", "actor", "scott glenn", "joe don baker",
			"REXX", "proglanguage", "exec2", "vhdl",
			"Transformers: Revenge of the Fallen", "movie", "star trek",
			"the punisher" };

	String[] siblingConceptIncorrect = new String[] { "Fortress",
			"basicfood|mountain", "kale", "mountain", "Stories",
			"cartooncharacter|award", "character", "harvey awards", "secure",
			"movie|painter", "die hard", "painter", "coat of arms",
			"soccerclub|sportevent", "football team", "rugby world cup sevens",
			"an intimate experience", "worldwarbattle|movie", "battle",
			"spy kids", "Shanghai", "city|nationalpark", "nanjing",
			"national park", "New York Jets", "nbateam|stadium",
			"new york knicks", "stadium", "Candy cane",
			"flower|cartooncharacter", "poinsettia", "the grinch",
			"Cleveland Cavaliers", "country|nbateam", "ireland",
			"denver nuggets", "mushrooms", "worldwarbattle|videogame",
			"battle", "kingdom hearts ii", "", "cartooncharacter|treaty",
			"winnie the pooh", "treaty", "Music", "movie|sportevent",
			"wedding crashers", "sport", "NBA", "holiday|nbateam", "holiday",
			"boston celtics", "Castle", "river|city", "river", "prague",
			"dlxdemo", "videogame|empire", "forza motorsport", "empire",
			"Argentina", "sportevent|country", "copa libertadores", "country",
			"Password", "country|videogame", "country", "the sims", "Asia",
			"country|mountain", "laos", "mountain", "Cave National Park",
			"city|nationalpark", "city", "badlands national park", "Park",
			"river|university", "housatonic river", "university" };

	/**
	 * @throws Exception
	 * 
	 */
	public SealConstraints(String indexDir, String categoryMapping,
			String titleMapping, int K) throws Exception {
		mri = new MainRelationIdentification(indexDir, categoryMapping,
				titleMapping, K);
	}

	/**
	 * 
	 * @param relation
	 * @param concept1
	 * @param concept2
	 * @param conceptX
	 * @return 1: satisfied the constraints; 0: unsatisfied the constraints;
	 * @throws Exception
	 */
	public int checkConstraints(int relation, String concept1, String concept2,
			String conceptX) throws Exception {
		if (relation == RelationIdentification.ANCESTOR_E1_TO_E2
				|| relation == RelationIdentification.ANCESTOR_E2_TO_E1) {
			return 1;
		} else if (relation == RelationIdentification.COUSIN) {
			int relX = mri.identifyPair(concept1, conceptX);
			int relY = mri.identifyPair(concept2, conceptX);
			if (relX == RelationIdentification.COUSIN
					&& relY == RelationIdentification.COUSIN)
				return 1;
			else {
				System.out.println("\trelX = " + relX);
				System.out.println("\trelY = " + relY);
				return 0;
			}
		}

		return 1; // satisfied the constraints
	}

	public void runCheckingContraints(String[] pairs) throws Exception {
		int size = pairs.length;

		if (size % 4 != 0) {
			System.out.println("ERROR: size % 4 != 0, size = " + size);
			System.exit(1);
		}
		int i = 0;
		int total = 0;
		int zero = 0;
		int one = 0;
		int nothing = 0;
		while (i < size) {
			String conceptX = pairs[i++].trim();
			String conceptY = pairs[i++].trim();
			String concept1 = pairs[i++].trim();
			String concept2 = pairs[i++].trim();
			total ++;
			if (conceptX.length() == 0) {
				System.out.println("\tConceptX is empty.");
				nothing ++;
				continue;
			}
			System.out.println("Concept1: " + concept1);
			System.out.println("Concept2: " + concept2);
			System.out.println("ConceptX: " + conceptX);
			int result = checkConstraints(RelationIdentification.COUSIN,
					concept1, concept2, conceptX);
			if (result == 1) {
				System.out.println("result = 1");
				one ++;
			} else {
				System.out.println("result = 0");
				zero ++;
			}
			if (i >= size)
				break;
		}
		System.out.println("- total: " + total);
		System.out.println("- 1: " + one);
		System.out.println("- 0: " + zero);
		System.out.println("- _: " + nothing);
	}
	
	public void runAll() throws Exception {
		System.out.println("---- Checking siblingConceptCorrect");
		runCheckingContraints(siblingConceptCorrect);
		System.out.println("---- Checking siblingConceptIncorrect");
		runCheckingContraints(siblingConceptIncorrect);
	}
}
