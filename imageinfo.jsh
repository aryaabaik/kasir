import javax.imageio.ImageIO;
import java.io.File;
for (var p : args) { var i=ImageIO.read(new File(p)); System.out.println(p+" "+i.getWidth()+"x"+i.getHeight()); }
