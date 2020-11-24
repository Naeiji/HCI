package Snake;

import java.awt.Image;

import javax.swing.ImageIcon;

public class Wall {
	private int[] wallX = new int[100];
	private int[] wallY = new int[100];
	private Image image;

	public Wall(){
		//Load image
		ImageIcon ii1 = new ImageIcon("src/Snake/images/wall.png");
		image = ii1.getImage();
	}
	
	public void random(){
		int x = (int) ((Math.random() * 10) + 4) * 20;
		int y = (int) ((Math.random() * 10) + 1) * 20;
		
		for (int i = 0; i < 4; i++) {
			wallX[i] = x + i * 20;
			wallY[i] = y;
		}
		
		x = (int) ((Math.random() * 20) + 25) * 20;
		y = (int) ((Math.random() * 10) + 12) * 20;
		
		for (int i = 0; i < 4; i++) {
			wallX[i + 4] = x;
			wallY[i + 4] = y + i * 20;
		}
		
		x = (int) ((Math.random() * 10) + 5) * 20;
		y = (int) ((Math.random() * 6) + 9) * 20;
		
		for (int i = 0; i < 5; i++) {
			wallX[i + 8] = x;
			wallY[i + 8] = y + i * 20;
		}

	}
	
	public Image getImage(){
		return image;
	}
	
	public int getLength(){
		return 12;
	}
	
	public int[] getX(){
		return wallX;
	}
	
	public int[] getY(){
		return wallY;
	}
}
