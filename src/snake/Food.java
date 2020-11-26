package snake;

import java.awt.Image;

import javax.swing.ImageIcon;


public class Food {
	private int foodX;
	private int foodY;
	private int regimenX;
	private int regimenY;
	private int foodCounter = 0;
	
	private Image[] food;
	private Image   foodIMG;
	private Image   regimenIMG;

	public Food(){
		food = new Image[2];
		//load Images
		ImageIcon ii1 = new ImageIcon("src/Snake/images/apple.png");
		food[0] = ii1.getImage();
		
		ImageIcon ii2 = new ImageIcon("src/Snake/images/chili.png");
		food[1] = ii2.getImage();
		
		ImageIcon ii3 = new ImageIcon("src/Snake/images/Regimen.png");
		regimenIMG = ii3.getImage();
	}
	
	public void random(){
		foodIMG = food[(int) (Math.random() * 2)];
		
		foodX = (int) (Math.random() * 47) + 1;
		foodX = foodX * 20;
		
		foodY = (int) (Math.random() * 34) + 1;
		foodY = foodY * 20;
		
		if(foodCounter % 5 == 0){
			regimenX = (int) (Math.random() * 47) + 1;
			regimenX = regimenX * 20;
			
			regimenY = (int) (Math.random() * 34) + 1;
			regimenY = regimenY * 20;
		}
	}
	
	public Image getImage(){
		return foodIMG;
	}
	
	public Image getRegimen(){
		return regimenIMG;
	}
	
	public int getRegimenX(){
		return regimenX;
	}
	
	public int getRegimenY(){
		return regimenY;
	}
	
	public int getX(){
		return foodX;
	}
	
	public int getY(){
		return foodY;
	}
	
}
