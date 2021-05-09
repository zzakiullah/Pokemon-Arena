/*
 * PokemonArena.java
 * Zulaikha Zakiullah
 * This program makes a text-based game called Pokemon Arena.
 * The player can choose 4 Pokemon from a list to battle and defeat the rest and be called "Trainer Supreme".
 */
 
import java.util.*;
import java.awt.*;
import java.io.*;

class Pokemon {
	private int initHP, hp, energy;  							 // initHP - initial HP; hp - current HP, cannot exceed initHP
	private String name, type, resistance, weakness;
	private boolean stunned = false, disabled = false;  		 // flags used to indicate whether Pokemon has been stunned or disabled
	private ArrayList<Attack> attacks = new ArrayList<Attack>(); // Pokemon's different attacks, held as Attack objects
	
	public Pokemon(String [] info) {  // takes in an array of Pokemon's stats from text file
		name = info[0];
		initHP = hp = Integer.parseInt(info[1]);
		energy = 50;
		type = info[2];
		resistance = info[3];
		weakness = info[4];
		for (int i=0; i<(Integer.parseInt(info[5])*4); i+=4) {
			Attack atk = new Attack(info[6+i], Integer.parseInt(info[7+i]), Integer.parseInt(info[8+i]), info[9+i]);
			attacks.add(atk);
		}
	}
	public Pokemon() {
		name = type = resistance = weakness = "";
		hp = energy = 0;
	}
	
	// returning Pokemon's fields
	public String getName() {
		return name;
	}
	public int getHP() {
		return hp;
	}
	public int getEnergy() {
		return energy;
	}
	public String getType() {
		return type;
	}
	public String getResistance() {
		return resistance;
	}
	public String getWeakness() {
		return weakness;
	}
	public boolean getStunStat() {
		return stunned;
	}
	public boolean getDisableStat() {
		return disabled;
	}
	public ArrayList<Attack> getAttacks() {
		return attacks;
	}
	
	public void setHP(int newHP) {
		if (newHP < 0) {
			hp = 0;
		}
		else if (newHP > initHP) {
			hp = initHP;
		}
		else {
			hp = newHP;
		}
	}
	
	public void setEnergy(int newEnergy) {
		if (newEnergy > 50) {
			energy = 50;
		}
		else if (newEnergy < 0) {
			energy = 0;
		}
		else {
			energy = newEnergy;
		}
	}
	
	public void releaseStunned() {
		stunned = false;
	}
	public void releaseDisabled() {
		if (disabled) {
			disabled = false;
			for (Attack atk : attacks) {
				atk.damage += 10;
			}
		}
	}
	
	public boolean canAttack() {  // check if Pokemon can use any of its attacks
		for (Attack atk : attacks) {
			if (energy >= atk.getEnergyCost()) {
				return true;
			}
		}
		return false;
	}
	
	private boolean success() {  // used in Stun, Wild Card, and Wild Storm attacks, to simulate 50% chance of attacks succeeding and 50% chance failing
		Random rand = new Random();
		return (rand.nextInt() >= 0.5);
	}
	
	public String attack(Attack atkUsed, Pokemon enemy) {  // used when Pokemon is attacking another Pokemon
		// atkUsed - attack object Pokemon has used; enemy - Pokemon's opponent
		// returns a string containing information used to write on console in public class
		
		double damageMultiplier = 1;  // amount damage has been multiplied depending on Pokemon's resistance and weakness
		String stat = " ";
		int damageDealt = 0;  // damageDealt - amount of damage Pokemon currently attacking has given to enemy
	
		if (enemy.getResistance().equals(type)) {
			damageMultiplier *= 0.5;
		}
		else if (enemy.getWeakness().equals(type)) {
			damageMultiplier *= 2;
		}
		
		if (atkUsed.getSpecial().equals("Stun")) {  // if attack's special is stun
			damageDealt += atkUsed.getDamage()*damageMultiplier;
			enemy.setHP((int)(enemy.hp-atkUsed.getDamage()*damageMultiplier));
			this.setEnergy(energy-atkUsed.getEnergyCost());
			if (success()) {
				stat = "succeeded";
				enemy.stunned = true;
			}
			else {
				stat = "failed";
			}
		}
		else if (atkUsed.getSpecial().equals("Wild Card")) {  // if attack's special is wild card
			if (success() && energy >= atkUsed.getEnergyCost()) {
				stat = "succeeded";
				damageDealt += atkUsed.getDamage()*damageMultiplier;
				enemy.setHP((int)(enemy.hp-atkUsed.getDamage()*damageMultiplier));
				this.setEnergy(energy-atkUsed.getEnergyCost());
			}
			else {
				stat = "failed";
			}
		}
		else if (atkUsed.getSpecial().equals("Wild Storm")) {  // if attack's special is wild storm
			boolean wildStorming = true, firstTime = true;
			while (wildStorming) {
				if (success()) {
					if (firstTime) {
						this.setEnergy(energy-atkUsed.getEnergyCost());  // if the attack succeeds the first time, energy is deducted
						firstTime = false;
					}
					damageDealt += atkUsed.getDamage()*damageMultiplier;
					enemy.setHP((int)(enemy.hp-atkUsed.getDamage()*damageMultiplier));
				}
				else {
					wildStorming = false;
				}
			}
		}
		else if (atkUsed.getSpecial().equals("Disable")) {  // if attack's special is disable
			if (enemy.disabled == false) {  // Pokemon can only be disabled once, so if he/she is not previously disabled, his/her attacks' damages
				enemy.disabled = true;      // are deducted by 10
 				for (Attack atk : enemy.attacks) {
					if (atk.damage-10 < 0) {
						atk.damage = 0;
					}
					else {
						atk.damage -= 10;
					}
				}
			}
			damageDealt += atkUsed.getDamage()*damageMultiplier;
			enemy.setHP((int)(enemy.hp-atkUsed.getDamage()*damageMultiplier));
			this.setEnergy(energy-atkUsed.getEnergyCost());
		}
		else if (atkUsed.getSpecial().equals("Recharge")) { // if attack's special is recharge
			damageDealt += atkUsed.getDamage()*damageMultiplier;
			enemy.setHP((int)(enemy.hp-atkUsed.getDamage()*damageMultiplier));
			this.setEnergy(energy-atkUsed.getEnergyCost()+20);  // add 20 to energy because of recharge
		}
		else {  // if attack has no special
			damageDealt += atkUsed.getDamage()*damageMultiplier;
			enemy.setHP((int)(enemy.hp-atkUsed.getDamage()*damageMultiplier));
			this.setEnergy(energy-atkUsed.getEnergyCost());
		}
		return atkUsed.getSpecial()+","+stat+","+Integer.toString(damageDealt);
	}
}


class Attack {   // class to deal with Pokemon's attacks
	String name, special;
	int energyCost, damage;
	
	public Attack(String name, int energyCost, int damage, String special) {
		this.name = name;
		this.energyCost = energyCost;
		this.damage = damage;
		this.special = special;
	}
	public Attack() {
		this.name = "";
		this.energyCost = 0;
		this.damage = 0;
		this.special = "";
	}
	
	// methods that return fields
	public String getName() {
		return name;
	}
	public int getEnergyCost() {
		return energyCost;
	}
	public int getDamage() {
		return damage;
	}
	public String getSpecial() {
		return special;
	}
}


public class PokemonArena {  // where the game takes place
	public static ArrayList<Pokemon> inGame = new ArrayList<Pokemon>();  // player's Pokemon that are still awake
	public static ArrayList<Pokemon> enemies = new ArrayList<Pokemon>(); // enemy Pokemon
	
	public static void main(String [] args) {
		makePokemon();
		startGame();
		displayPokemon(enemies, "<<POKEMON ROSTER>>\n");
		choosePokemon();
		battle();
	}
    
    public PokemonArena() {
    }
    
    public static void makePokemon() {  // read from text file and make Pokemon objects
		try {
			Scanner inFile = new Scanner(new File("pokemon.txt"));
			int n = Integer.parseInt(inFile.nextLine());
			for (int i=0; i<n; i++) {
				Pokemon poke = new Pokemon(inFile.nextLine().split(","));
				enemies.add(poke);
			}
		}
		catch (IOException ex){
			System.out.println("Could not read file. Try again.");
		}
    }
	
	public static void startGame() {  // start of game
		System.out.print("Welcome to Pokemon Arena! ");
		userEnter("start");
		System.out.print("\n");
	}
	
	public static void userEnter(String action) {  // allows user to press enter to continue (added or else too much text is added at a time during game)
 		Scanner kb = new Scanner(System.in);
		System.out.print("(Press ENTER to "+action+") ");
		kb.nextLine();
	}
	
	/* writeStat - used for writing Pokemon's stats using printf
	 * pos - position relative to ArrayList of objects (Pokemon, Attack); initSpace - initial space at beginning of line after gap
	 * gap - space between previous stat in same row to current stat; statName - stat type (HP, Energy, etc.); stat - Pokemon's actual stat
	 * 2 writeStat methods: first where stat is a String, and second where stat is an int
	 */
	public static int writeStat(int pos, String initSpace, int gap, String statName, String stat) {
		if (pos == 0) {
			System.out.print("\n");
			gap = 0;
		}
		System.out.printf(initSpace+"%"+(gap+statName.length())+"s %s", statName, stat);
		return 26-initSpace.length()-statName.length()-stat.length();
	}
	public static int writeStat(int pos, String initSpace, int gap, String statName, int stat) {
		if (pos == 0) {
			System.out.print("\n");
			gap = 0;
		}
		System.out.printf(initSpace+"%"+(gap+statName.length())+"s %d", statName, stat);
		return 26-initSpace.length()-statName.length()-Integer.toString(stat).length();
	}
	
    public static void displayPokemon(ArrayList<Pokemon> pokes, String title) {  // display Pokemon and their stats
    	// pokes - ArrayList of Pokemon;
    	System.out.println(title);
    	int row = 6;  // row - number of Pokemon stats displayed per row
    	for (int i=0; i<pokes.size(); i+=row) {
    		int gap = 0;  // gap - space between text in the same row
    		if (i+6 > pokes.size()) {
    			row = pokes.size()-i;
    		}
    		for (int j=0; j<row; j++) {
    			System.out.printf("%"+(gap+(Integer.toString(j+1)).length())+"d) %"+pokes.get(i+j).getName().length()+"s", i+j+1, pokes.get(i+j).getName());
    			gap = 25-Integer.toString(j+1).length()-pokes.get(i+j).getName().length();
    		}
    		for (int j=0; j<row; j++) {
    			gap = writeStat(j, "     ", gap, "HP:", pokes.get(i+j).getHP());
    		}
    		for (int j=0; j<row; j++) {
    			gap = writeStat(j, "     ", gap, "Energy:", pokes.get(i+j).getEnergy());
    		}
    		for (int j=0; j<row; j++) {
    			gap = writeStat(j, "     ", gap, "Type:", pokes.get(i+j).getType().substring(0,1).toUpperCase()+pokes.get(i+j).getType().substring(1));
    		}
    		for (int j=0; j<row; j++) {
    			gap = writeStat(j, "     ", gap, "Resistance:", pokes.get(i+j).getResistance().substring(0,1).toUpperCase()+pokes.get(i+j).getResistance().substring(1));
    		}
    		for (int j=0; j<row; j++) {
    			gap = writeStat(j, "     ", gap, "Weakness:", pokes.get(i+j).getWeakness().substring(0,1).toUpperCase()+pokes.get(i+j).getWeakness().substring(1));
    		}
    		for (int j=0; j<row; j++) {
    			gap = writeStat(j, "     ", gap, "Attacks:", pokes.get(i+j).getAttacks().size());
    		}
    		System.out.println("\n");
	   	}
    }
    
    public static void choosePokemon() {  // allow user to choose its 4 Pokemon
    	Scanner kb = new Scanner(System.in);
    	int pokePos;
    	System.out.println("Choose 4 Pokemon:");
    	for (int j=1; j<5; j++) {
    		pokePos = kb.nextInt();
    		if (pokePos >= 1 && pokePos <= enemies.size() && !(inGame.contains(enemies.get(pokePos-1)))) {
    			inGame.add(enemies.get(pokePos-1));
    			System.out.println("You have chosen " + enemies.get(pokePos-1).getName() + ".");
    		}
    		else if (pokePos >= 1 && pokePos <= enemies.size()) {  // if the player chooses a Pokemon he/she has already chosen
    			j -= 1;  // loop must go back around one more time so player can choose 4 Pokemon
    			System.out.println("You've already chosen " + enemies.get(pokePos-1).getName() + ". You still need to choose " + Integer.toString(4-j) + " more Pokemon.");
    		}
    		else {
    			j -= 1;
    			System.out.println("I am sorry, that Pokemon cannot be found. You still need to choose " + Integer.toString(4-j) + " more Pokemon.");
    		}
    	}
    	System.out.print("\n");
    	for (Pokemon p : inGame) {
    		enemies.remove(p);
    	}
    }
    
    public static Pokemon chooseOnePoke() {  // used when a player needs to choose a Pokemon
    	Scanner kb = new Scanner(System.in);
    	int chosenPos; // number which player chose as the Pokemon they will use
    	System.out.println("Choose a Pokemon:\n");
    	displayPokemon(inGame, "<<YOUR POKEMON>>");
		while (true) {
			chosenPos = kb.nextInt()-1;
			if (chosenPos >= 0 && chosenPos <= inGame.size()-1) {
				return inGame.get(chosenPos);
			}
			else {
				System.out.println("Pokemon not found. Try again.");
			}
		}
    }
    
    public static int playerTurn() {          // used when it is player's turn and they must choose a number
    	Scanner kb = new Scanner(System.in);
       	return kb.nextInt();
    }
    
    public static int enemyTurn(int range) {  // used when it is enemy's turn and a number must be randomly chosen
    	Random rand = new Random();
    	return rand.nextInt(range)+1;
    }
    
    public static Pokemon chooseMove(Pokemon pokeTurn, Pokemon pokeNotTurn) {  // player or enemy must choose to attack, retreat, or pass
    	int ATTACK = 1, RETREAT = 2, PASS = 3, chosenPos, move, timesChoosing = 1;
    	boolean displayOnce = true;  // flag to make sure action choices are only be shown once
		while (true) {
			if (inGame.contains(pokeTurn)) {  // indicates that it is player's turn
				if (displayOnce) {
					System.out.print("Choose an action:\n1)\tAttack");
					if (!(pokeTurn.canAttack()) || pokeTurn.getStunStat()) {
						System.out.print(" (Invalid)\n2)\tRetreat");
					}
					else {
						System.out.print("\n2)\tRetreat");
					}
					if (pokeTurn.getStunStat()) {
						System.out.println(" (Invalid)\n3)\tPass");
					}
					else {
						System.out.println("\n3)\tPass");
					}
				}
				move = playerTurn();
			}
			else {							// indicates that it is enemy's turn
				move = enemyTurn(3);
			}
			if (move == ATTACK && pokeTurn.canAttack() && pokeTurn.getStunStat() == false) {
				Attack atk = chooseAttack(pokeTurn, pokeNotTurn);
				
				boolean initDisabled = false;  // flag for whether Pokemon has already been disabled or not
				if (atk.getSpecial().equals("Disable") && pokeNotTurn.getDisableStat()) {
					initDisabled = true;
				}
				
				String [] status = pokeTurn.attack(atk, pokeNotTurn).split(",");
				if (status[0].equals("Stun")) {
					System.out.println(pokeTurn.getName()+" has attacked with "+atk.getName()+" and dealt "+status[2]+" damage!\n"+pokeTurn.getName()+" has "+status[1]+" in stunning "+pokeNotTurn.getName()+"!");
				}
				else if (status[0].equals("Wild Card")) {
					System.out.println(pokeTurn.getName()+" has "+status[1]+" in a Wild Card attack, "+atk.getName()+", and has dealt "+status[2]+" damage!");
				}
				else if (status[0].equals("Wild Storm")) {
					System.out.println(pokeTurn.getName()+" has used a Wild Storm attack, "+atk.getName()+", and has dealt "+status[2]+" damage!");
				}
				else if (status[0].equals("Disable")) {
					System.out.println(pokeTurn.getName()+" has attacked with "+atk.getName()+" and dealt "+status[2]+" damage!");
					if (initDisabled) {
						System.out.println(pokeNotTurn.getName()+" has already been disabled!");
					}
					else {
						System.out.println(pokeTurn.getName()+" has disabled "+pokeNotTurn.getName()+"!");
					}
				}
				else if (status[0].equals("Recharge")) {
					System.out.println(pokeTurn.getName()+" has attacked with "+atk.getName()+" and dealt "+status[2]+" damage!\n"+pokeTurn.getName()+" has recharged!");
				}
				else {
					System.out.println(pokeTurn.getName()+" has attacked with "+atk.getName()+" and dealt "+status[2]+" damage!");
				}
				return pokeTurn;
			}
			else if (move == RETREAT && pokeTurn.getStunStat() == false && inGame.contains(pokeTurn)) {
				System.out.println(pokeTurn.getName()+" is retreating!");
				return chooseOnePoke();
			}
			else if (move == PASS) {
				System.out.println(pokeTurn.getName()+" is passing!");
				return pokeTurn;
			}
			else if (((move == ATTACK && (pokeTurn.canAttack() == false || pokeTurn.getStunStat())) || (move == RETREAT && pokeTurn.getStunStat())) && inGame.contains(pokeTurn)) {
				System.out.println("You cannot use this move. Choose again.");
			}
			else if (inGame.contains(pokeTurn) && move != ATTACK && move != RETREAT && move != PASS) {
				System.out.println("That is not a valid move. Choose again.");
			}
			displayOnce = false;
    	}
    }
    
    public static void displayAttacks(ArrayList<Attack> atks, String title) {  // used to show user Pokemon's different attacks
    	System.out.println(title);
    	int row = 6;  // number of attacks displayed per row
    	for (int i=0; i<atks.size(); i+=row) {
    		int gap = 0; // gap - space between text in the same row
    		if (i+6 > atks.size()) {
    			row = atks.size()-i;
    		}
    		for (int j=0; j<row; j++) {
    			System.out.printf("%"+(gap+(Integer.toString(j+1)).length())+"d) %"+atks.get(i+j).getName().length()+"s", i+j+1, atks.get(i+j).getName());
    			gap = 25-Integer.toString(j+1).length()-atks.get(i+j).getName().length();
    		}
    		for (int j=0; j<row; j++) {
    			gap = writeStat(j, "     ", gap, "Energy Cost:", atks.get(i+j).getEnergyCost());
    		}
    		for (int j=0; j<row; j++) {
    			gap = writeStat(j, "     ", gap, "Damage:", atks.get(i+j).getDamage());
    		}
    		for (int j=0; j<row; j++) {
    			gap = writeStat(j, "     ", gap, "Special:", atks.get(i+j).getSpecial());
    		}
    		System.out.println("\n");
    	}
    }
    
    public static Attack chooseAttack(Pokemon pokeTurn, Pokemon pokeNotTurn) {  // used when player or enemy must choose an attack
    	Scanner kb = new Scanner(System.in);
		if (inGame.contains(pokeTurn)) {  // if it is player's turn
			displayAttacks(pokeTurn.getAttacks(), "\n<"+pokeTurn.getName()+"'s Attacks>");
			System.out.println("Choose an attack:");
			while (true) {
				int move = playerTurn();
				if (move >= 1 && move <= pokeTurn.getAttacks().size() && pokeTurn.canAttack()) {
					return pokeTurn.getAttacks().get(move-1);
				}
				else if (move >= 1 && move <= pokeTurn.getAttacks().size() && pokeTurn.canAttack() == false) {
					System.out.println("You don't have enough energy to use this attack. Choose again.");
				}
				else {
					System.out.println("That is not a valid attack. Choose again.");
				}
			}
		}
		else {  // if it is enemy's turn
			while (true) {
				int move = enemyTurn(pokeTurn.getAttacks().size());
				if (pokeTurn.canAttack() && pokeTurn.getEnergy() >= pokeTurn.getAttacks().get(move-1).getEnergyCost()) {
					return pokeTurn.getAttacks().get(move-1);
				}
			}
		}	
    }
    
    public static void displayStatus(Pokemon [] pokes, String title) {  // displays name, HP, energy, and stun and disable state of the 2 Pokemon battling
    	System.out.println(title);
    	int gap = 0;
    	for (int i=0; i<pokes.length; i++) {
    		System.out.printf("%"+(gap+(Integer.toString(i+1)).length())+"d) %"+pokes[i].getName().length()+"s", i+1, pokes[i].getName());
    		gap = 24-pokes[i].getName().length();
    	}
    	for (int i=0; i<pokes.length; i++) {
    		gap = writeStat(i, "     ", gap, "HP:", pokes[i].getHP());
    	}
    	for (int i=0; i<pokes.length; i++) {
    		gap = writeStat(i, "     ", gap, "Energy:", pokes[i].getEnergy());
    	}
    	for (int i=0; i<pokes.length; i++) {
    		if (pokes[i].getStunStat()) {
    			gap = writeStat(i, "     ", gap, "Stunned:", "Yes");
    		}
    		else {
    			gap = writeStat(i, "     ", gap, "Stunned:", "No");
    		}
    	}
    	for (int i=0; i<pokes.length; i++) {
    		if (pokes[i].getDisableStat()) {
    			gap = writeStat(i, "     ", gap, "Disabled:", "Yes");
    		}
    		else {
    			gap = writeStat(i, "     ", gap, "Disabled:", "No");
    		}
    	}
    	System.out.print("\n");
    }
    
    public static void battle() {  // where Pokemon battling takes place
    	String turn; // turn can either be "player" or "enemy"
    	Pokemon yourPoke = new Pokemon();  // player's Pokemon
    		   
		Collections.shuffle(enemies);  // randomize order to battle Pokemon
    	int roundNum = 0;  // roundNumber
    	
    	while (enemies.size() > 0 && inGame.size() > 0) {
    		roundNum += 1;
    		
    		Pokemon enemy = enemies.get(0);
    		
    		System.out.println("You will be fighting "+enemy.getName()+".\n");
			
			yourPoke = chooseOnePoke();
			
			System.out.println("\nRound " + Integer.toString(roundNum) + ": " + yourPoke.getName() + " vs. " + enemy.getName()+"\n");
			
			Random rand = new Random();
			double firstTurn = rand.nextInt(); // randomize who goes first
			if (firstTurn >= 0.5) { 
				turn = "enemy";
				System.out.println(enemy.getName()+" will be going first.");
			}
			else {
				turn = "player";
				System.out.println("You will be going first.");
			}
			userEnter("continue");
			
			Pokemon [] p = {yourPoke, enemy};
			displayStatus(p, "<INITIAL STATUS>");
			userEnter("continue");
			
			int turnNum = 0; // indicates how many turns have passed; every 2 turns indicates a round
			boolean fighting = true, initStunned = false; // initStunned - flag to indicate whether a Pokemon has been stunned from opponent's turn or not
			while (fighting) {				
				if (turn.equals("player")) {  // if it is player's turn
					yourPoke = chooseMove(yourPoke, enemy);
					if (enemy.getStunStat() && initStunned == false) { // if enemy has been stunned by player
						initStunned = true;
					}
					if (yourPoke.getStunStat() && initStunned) {  // if player has been stunned by enemy
						initStunned = false;
						yourPoke.releaseStunned();  // player is no longer stunned as his/her turn has finished
					}
				}
				else {						  // if it is enemy's turn
					enemy = chooseMove(enemy, yourPoke);
					if (yourPoke.getStunStat() && initStunned == false) { // if player has been stunned by enemy
						initStunned = true;
					}
					if (enemy.getStunStat() && initStunned) {  // if enemy has been stunned by player
						initStunned = false;
						enemy.releaseStunned();  // enemy is no longer stunned as his/her turn has finished
					}
				}
				
				turnNum += 1;
				if (turnNum % 2 == 0 && turnNum != 0) {  // indicates a round has been done, add 10 energy to player's Pokemon and the enemy currently in battle
					for (Pokemon poke : inGame) {
						poke.setEnergy(poke.getEnergy()+10);
					}
					enemy.setEnergy(enemy.getEnergy()+10);
				}
				
				if (yourPoke.getHP() == 0) {  // if player's Pokemon has been KO'ed
					inGame.remove(yourPoke);
					System.out.println(yourPoke.getName()+" has been KO'ed!");
					userEnter("continue");
					if (inGame.size() != 0) {  // if there are any of player's Pokemon left
						System.out.println("Choose another Pokemon:");
						yourPoke = chooseOnePoke();
					}
				}
				else if (enemy.getHP() == 0) {  // if enemy's Pokemon has been KO'ed
					System.out.println(enemy.getName()+" has been KO'ed! "+yourPoke.getName()+" wins Round "+Integer.toString(roundNum)+"!");
					userEnter("continue");
					fighting = false;
					enemies.remove(0);
				}
				else {
					turn = turn.equals("player") ? "enemy" : "player";  // switch turn
					Pokemon [] pokes = {yourPoke, enemy};
					displayStatus(pokes, "<STATUS UPDATE>");
					userEnter("continue");
				}
			}
			for (Pokemon poke : inGame) {
				poke.setHP(poke.getHP()+20);
				poke.releaseDisabled(); // Pokemon is no longer disabled
			}
		}
		if (inGame.size() != 0) {
			System.out.println("Congratulations, I dub thee Trainer Supreme!");
		}
		else {
			System.out.println("It appears you are not ready to hold the title of Trainer Supreme.");		
		}	
    }
}