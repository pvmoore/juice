package juice;

import juice.Lambda.AAR;
import juice.Lambda.AR;
import juice.types.Float2;
import juice.types.Rect;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

final public class Font {
    public String name;
    public int size, width, height, lineHeight;
    public Page page;
    public Texture texture;

    @Override public String toString() {
        return String.format("[Font %s size: %d width:%d height:%d chars:%d kernings:%d]",
                             name, size, width, height, page.chars.size(), page.kernings.size());
    }
    Float2 getDimension(String text, float size) {
        return getRect(text, size).floatDimension();
    }
    Rect<Float> getRect(String text, float size) {
        var r = new Rect<>(0f,0f,0f,0f);
        if(text.length() == 0) return r;
        r.x = 1000f;
        r.y = 1000f;
        float X = 0;
        int i = 0;
        for(var ch : text.toCharArray()) {
            var g		= page.getChar(ch);
            float ratio = (size / (float)this.size);

            float x  = X + g.xoffset * ratio;
            float y  = 0 + g.yoffset * ratio;
            float xx = x + g.width * ratio;
            float yy = y + g.height * ratio;

            if(x<r.x) r.x = x;
            if(y<r.y) r.y = y;
            if(xx > r.w) r.w = xx;
            if(yy > r.h) r.h = yy;

            int kerning = 0;
            if(i+1<text.length()) {
                kerning = page.getKerning(ch, text.charAt(i + 1));
            }
            X += (g.xadvance + kerning) * ratio;
        }
        return r;
    }
    //=====================================================================================

    public static final class Char {
        public int id;
        public float u, v, u2, v2;
        public int width, height;
        public int xoffset, yoffset;
        public int xadvance;
    };
    public static final class Page {
        Map<Integer,Char> chars     = new HashMap<>();
        Map<Long, Integer> kernings = new HashMap<>(); /// key = (from<<32 | to)

        public int getKerning(int from, int to) {
            var k = kernings.get(((long)from) << 32 | to);
            if(k == null) k = 0;
            return k;
        }
        public Char getChar(int ch) {
            var c = chars.get(ch);
            if(c==null) c = chars.get(' ');
            return c;
        }
    }

    //====================================================================================
    public static void setDirectory(String directory) {
        Font.directory = directory + (directory.endsWith("/") ? "" : "/");
    }
    public static void destroy() {
        for(var e : map.entrySet()) {

        }
    }
    public static Font get(String name) {
        Font f = map.get(name);
        if(f==null) {
            f = load(name);
            map.put(name, f);
        }
        return f;
    }
    //====================================================================================
    private static String directory     = "./";
    private static Map<String,Font> map = new HashMap<>();
    private static final int BM_WIDTH   = 512;

    private static Font load(String name) {
        Font f    = new Font();
        f.name    = name;
        f.texture = Texture.get(directory, name+".png", Texture.standardAttribs);
        f.page    = readPage(f, name);
        return f;
    }
    private static Page readPage(Font font, String name) {
        AAR<String,Integer,String> getFirstToken = (line, offset)-> {
            var p = offset;
            while(p<line.length() && line.charAt(p)>32) p++;
            return line.substring(offset, p);
        };
        AAR<String,String,Integer> getInt = (line, key)-> {
            var p     = line.indexOf(key);
            var token = getFirstToken.call(line, (p+key.length()));
            return Integer.valueOf(token);
        };
        AR<String,Char> readChar = (line)-> {
            line = line.trim();

            var c = new Char();
            // assumes there are no spaces around '='
            var tokens = line.split("\\s+");
            var map    = new HashMap<String,String>();
            for(var it : tokens) {
                var pair = it.split("=");
                map.put(pair[0], pair[1]);
            }
            float x    = Float.valueOf(map.get("x"));
            float y    = Float.valueOf(map.get("y"));
            c.id       = Integer.valueOf(map.get("id"));
            c.width    = Integer.valueOf(map.get("width"));
            c.height   = Integer.valueOf(map.get("height"));
            c.xoffset  = Integer.valueOf(map.get("xoffset"));
            c.yoffset  = Integer.valueOf(map.get("yoffset"));
            c.xadvance = Integer.valueOf(map.get("xadvance"));
            c.u = x / BM_WIDTH;
            c.v = y / BM_WIDTH;
            c.u2 = (x + c.width - 1) / BM_WIDTH;
            c.v2 = (y + c.height - 1) / BM_WIDTH;
            return c;
        };

        Page page = new Page();
        try(var br = new BufferedReader(new FileReader(directory+name+".fnt"))) {
            String line;
            while((line=br.readLine())!=null) {
                line = line.trim();
                if(line.isEmpty()) continue;

                var firstToken = getFirstToken.call(line, 0);

                if(firstToken.equals("char")) {
                    var fc = readChar.call(line.substring(4));
                    page.chars.put(fc.id, fc);
                } else if(firstToken.equals("kerning")) {
                    long first  = getInt.call(line, "first=");
                    long second = getInt.call(line, "second=");
                    int amount  = getInt.call(line, "amount=");
                    page.kernings.put((first << 32) | second, amount);
                } else if(firstToken.equals("info")) {
                    font.size = getInt.call(line, "size=");
                } else if(firstToken.equals("common")) {
                    font.width      = getInt.call(line, "scaleW=");
                    font.height     = getInt.call(line, "scaleH=");
                    font.lineHeight = getInt.call(line, "lineHeight=");
                }
            }
        }catch(Exception e) {
            throw new RuntimeException(e);
        }

        return page;
    }
}
