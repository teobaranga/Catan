
public class Player {

	PlayerStatus status;
	private int defenderOfCatanVPs;
	private int gold;
	
	public Player(Account playerAccount) {
	
	}
	
	public PlayerStatus getPlayerStatus() {
		return status;
	}
	
	public void setPlayerStatus(PlayerStatus status) {
		this.status = status;
	}
		
	
	public int getDefenderOfCatanVPs() {
		return defenderOfCatanVPs;
	}
	
	public void setDefenderOfCatanVPs(int defenderOfCatanVPs) {
		this.defenderOfCatanVPs = defenderOfCatanVPs;
	}
	
	public int getGold() {
		return gold;
	}
	
	public void setGold(int gold) {
		this.gold = gold;
	}
	
		
}
