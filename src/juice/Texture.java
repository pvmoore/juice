package juice;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static juice.Util.exceptionContext;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

final public class Texture {
    public String name;
    public int width, height;
    public int id;
    public int format;

    public static Attribs standardAttribs = new Attribs(GL_CLAMP_TO_EDGE, GL_LINEAR);

    public static class Attribs {
        public int clamp;
        public int filter;

        public Attribs(int clamp, int filter) { this.clamp = clamp; this.filter = filter; }

        @Override public int hashCode() {
            return (Integer.hashCode(clamp) << 16) ^ (Integer.hashCode(filter));
        }
        @Override public boolean equals(Object obj) {
            var o = (Attribs)obj;
            return clamp == o.clamp && filter == o.filter;
        }
    }

    //====================================================================================
    public static void setDirectory(String directory) {
        Texture.directory = directory + (directory.endsWith("/") ? "" : "/");
    }
    public static void destroy() {
        for(var m : map.values()) {
            for(var t : m.entrySet()) {
                //System.out.println("Destroying texture '"+t.getKey()+"'");
                glDeleteTextures(t.getValue().id);
            }
        }
        map.clear();
    }

    public static Texture get(String filename, Attribs attribs) {
        return get(directory, filename, attribs);
    }
    public static Texture get(String directory, String filename, Attribs attribs) {
        if(!directory.endsWith("/")) directory += "/";
        final String dir = directory;

        var m = map.computeIfAbsent(attribs, k -> new HashMap<>());

        return m.computeIfAbsent(dir+filename, k-> load(dir, filename, attribs));
    }

    //====================================================================================
    private static String directory = "./";
    private static Map<Attribs, Map<String,Texture>> map = new HashMap<>();
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

    private static Texture load(String directory, String filename, Attribs attribs) {
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

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, attribs.clamp);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, attribs.clamp);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, attribs.filter);
        if(false) {
            // has mipmaps
            // todo - handle mipmaps
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        } else {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, attribs.filter);
        }

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.width, image.height, 0, image.format, GL_UNSIGNED_BYTE, bb);

        return image;
    }
}
