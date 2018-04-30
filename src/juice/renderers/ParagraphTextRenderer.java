package juice.renderers;

import juice.Font;
import juice.Lambda;
import juice.types.Float2;
import juice.types.Int2;
import juice.types.RGBA;
import juice.types.Rect;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

final public class ParagraphTextRenderer {
    private TextRenderer text;
    private Font font;
    private Rect<Integer> rect;
    private RGBA colour = RGBA.WHITE;
    private int size = 32;
    private int lineSpacing=4, wordSpacing=4;
    private Rect<Float> prevRect;

    private static final class Token {
        String s;
        Float2 dim;
        Token(String s, Float2 dim) { this.s = s; this.dim = dim; }
    }

    public ParagraphTextRenderer(Font font, Int2 pos, Int2 size) {
        this.font = font;
        this.rect = new Rect<>(pos.getX(), pos.getY(), size.getX(), size.getY());
        this.text = new TextRenderer(font);
        reset();
    }
    public void destroy() {
        text.destroy();
    }
    public ParagraphTextRenderer setVP(Matrix4f viewProj) {
        text.setVP(viewProj);
        return this;
    }
    public ParagraphTextRenderer setRect(Int2 pos, Int2 size) {
        this.rect = new Rect<>(pos.getX(), pos.getY(), size.getX(), size.getY());
        reset();
        return this;
    }
    public ParagraphTextRenderer setColour(RGBA c) {
        this.colour = c;
        text.setColour(c);
        return this;
    }
    public ParagraphTextRenderer setSpacing(int line, int word) {
        this.lineSpacing = line;
        this.wordSpacing = word;
        return this;
    }
    public ParagraphTextRenderer setSize(int size) {
        this.size = size;
        text.setSize(size);
        return this;
    }
    public ParagraphTextRenderer newLine() {
        // TODO - this won't work if prevRect size is 0
        prevRect.x  = (float)rect.x;
        prevRect.y += prevRect.h + lineSpacing;
        prevRect.w  = 0f;
        return this;
    }
    public ParagraphTextRenderer left(String str) {

        handleNewLines(str, (String s)->{
            for(var w : s.split("\\s+")) {
                writeWord(w, font.getDimension(w, size), wordSpacing);
            }
        });

        return this;
    }
    public ParagraphTextRenderer centred(String str) {
        var dim         = font.getDimension(str, size);
        float rightEdge = rect.x + rect.w;
        float leftEdge  = prevRect.x + prevRect.w;
        float remainder = (rightEdge-leftEdge)-dim.getX();
        if(remainder<0) {
            // we don't have enough room on the line
            return left(str);
        }
        prevRect.w += remainder/2;
        return left(str);
    }
    public ParagraphTextRenderer justified(String multilineString) {

        handleNewLines(multilineString, (String str)->{
            List<Token> tokens = new ArrayList<>();

            for(var w : str.split("\\s+")) {
                tokens.add(new Token(w, font.getDimension(w, size)));
            }

            while(tokens.size() > 0) {
                float leftEdge  = prevRect.x + prevRect.w;
                float rightEdge = rect.x + rect.w;
                int count       = countWordsUntilEOL(tokens, rightEdge - leftEdge);

                //log("para: count=%s token[0]=%s", count, tokens[0][0]);

                if(count==0 && tokens.get(0).dim.getX() > rect.w) {
                    // this word is too big for the rect width. just print it
                    count = 1;
                }

                if(count==0) {
                    // no words fit on this line. go to the next line
                    newLine();
                } else if(count==1) {
                    // 1 word fits on this line
                    writeWord(tokens.get(0).s, tokens.get(0).dim, wordSpacing);
                    tokens.remove(0);
                } else {
                    // justify
                    var combinedTextWidth = (float)
                        tokens.subList(0, count)
                              .stream()
                              .mapToDouble(it->it.dim.getX())
                              .sum();

                    var wordSpacing = ((rightEdge-leftEdge)-combinedTextWidth) / (count-1);

                    tokens.subList(0, count).forEach(it->writeWord(it.s, it.dim, wordSpacing));

                    tokens = tokens.subList(count, tokens.size());
                }
            }
        });

        return this;
    }
    public ParagraphTextRenderer clear() {
        text.clearText();
        reset();
        return this;
    }
    public void render() {
        text.render();
    }
    //======================================================================
    private void reset() {
        prevRect = new Rect<>((float)rect.x, (float)rect.y, 0f,0f);
    }
    private void handleNewLines(String str, Lambda.AV<String> lambda) {
        var lines = str.split("\n");
        for(int i=0; i<lines.length; i++) {
            lambda.call(lines[i]);
            if(i+1<lines.length) {
                newLine();
            }
        }
    }
    private int countWordsUntilEOL(List<Token> tokens, float remainingWidth) {
        int count = 0;
        for(var t : tokens) {
            remainingWidth -= (t.dim.getX() + wordSpacing);
            if(remainingWidth < 0) break;
            count++;
        }
        return count;
    };
    private void writeWord(String s, Float2 textDim, float wordSpacing) {
        float x = prevRect.x + prevRect.w;
        float y = prevRect.y;
        if(x + textDim.getX() > rect.x + rect.w) {
            // wrap
            x = rect.x;
            y += prevRect.h + lineSpacing;
        }

        prevRect = new Rect<>(x,y, textDim.getX() + wordSpacing, textDim.getY());

        text.appendText(s, new Int2((int)x, (int)y));
    }
}
