package juice;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import java.awt.Color;

import static juice.Util.exceptionContext;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

final public class Texture {
    public String name;
    public int width, height;
    public int id;
    public int format;

    //====================================================================================
    public static void setDirectory(String directory) {
        Texture.directory = directory + (directory.endsWith("/") ? "" : "/");
    }
    public static void destroy() {
        for(var t : map.entrySet()) {
            //System.out.println("Destroying texture '"+t.getKey()+"'");
            glDeleteTextures(t.getValue().id);
        }
    }

    public static Texture get(String filename) {
        return get(directory, filename);
    }
    public static Texture get(String directory, String filename) {
        if(!directory.endsWith("/")) directory += "/";

        var texture = map.get(directory+filename);
        if(texture==null) {
            texture = load(directory, filename);
            map.put(directory+filename, texture);
        }
        return texture;
    }

    //====================================================================================
    private static String directory = "./";
    private static Map<String,Texture> map = new HashMap<>();
    private static final ColorModel RGBAColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8,8,8,8},
                                                                             true,
                                                                             false,
                                                                             ComponentColorModel.TRANSLUCENT,
                                                                             DataBuffer.TYPE_BYTE);
    private static final ColorModel RGBColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                                                                            new int[] {8,8,8,0},
                                                                            false,
                                                                            false,
                                                                            ComponentColorModel.OPAQUE,
                                                                            DataBuffer.TYPE_BYTE);

    private static Texture load(String directory, String filename) {
        var image = new Texture();
        image.name = filename;

        var src = exceptionContext(() ->
            ImageIO.read(new File(directory + filename))
        );

        WritableRaster raster;
        BufferedImage texImage;
        image.width  = 2;
        image.height = 2;

        // Make the texture size a power of 2
        while(image.width < src.getWidth()) { image.width *= 2; }
        while(image.height < src.getHeight()) { image.height *= 2; }

        if(src.getColorModel().hasAlpha()) {
            image.format = GL_RGBA;
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,image.width,image.height,4,null);
            texImage = new BufferedImage(RGBAColorModel,raster,false,new Hashtable());
        } else {
            image.format = GL_RGB;
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,image.width,image.height,3,null);
            texImage = new BufferedImage(RGBColorModel,raster,false,new Hashtable());
        }

        // copy the source image into the produced image
        var g = texImage.getGraphics();
        g.setColor(new Color(0f,0f,0f,0f));
        g.fillRect(0,0,image.width,image.height);
        g.drawImage(src,0,0,null);


        byte[] bytes  = ((DataBufferByte)texImage.getData().getDataBuffer()).getData();
        ByteBuffer bb = Util.toBuffer(bytes);

        // Upload the texture to the GPU
        image.id = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, image.id);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        if(false) {
            // has mipmaps
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        } else {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        }

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.width, image.height, 0, image.format, GL_UNSIGNED_BYTE, bb);

        return image;
    }
}
