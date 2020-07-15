#version 430 core
#include "text.glsl"

uniform vec2 ScreenDimensions;
uniform vec2 ElementPosition;
uniform vec2 ElementDimensions;

uniform float TextFontScale;
uniform float TextLetterSpacing;
uniform int TextAlign;
uniform bool TextTransparentBg;
uniform int TextLineHeight;

out vec4 FragColor;

void main()
{
    vec2 fragCoord = gl_FragCoord.xy;

    // Figure out the index of which character we're looking at
    vec2 relativePixel = fragCoord - ElementPosition;
    float widthChars = font_nChars * TextLetterSpacing; // the width of all boxes, in pixels

    // Calculate extra space on the left side of the quad due to text alignment
    // If the text is right-aligned or center-aligned, there will be extra space there
    float extraSpace = ElementDimensions.x - widthChars; // if the text is right-aligned, this is the extra space at the
                                                         // left of the quad, where there aren't any characters

    float mulLut[3]; // multiply extraSpace by a number depending on whether it is left, right, or center aligned
    mulLut[0] = 0.0; // left-aligned: extraSpace should be zero
    mulLut[1] = 1.0; // right-aligned: extraSpace should be same
    mulLut[2] = 0.5; // center-aligned: extraSpace should be half
    extraSpace *= mulLut[TextAlign];

    // Calculate the index of the character in this box
    int charIndex = int(floor(( relativePixel.x - extraSpace ) / TextLetterSpacing));

    // Avoid out-of-range array accesses by setting charIndex to 0 if it is out-of-bounds
    bool wasCharIndexValid = charIndex >= 0 && charIndex < font_nChars;
    charIndex *= int(wasCharIndexValid);

    vec2 boxDimensions = vec2(TextLetterSpacing, TextLineHeight);
    vec2 boxTopLeft = ElementPosition + vec2(charIndex * TextLetterSpacing + extraSpace, 0);
    vec2 boxBottomRight = boxTopLeft + vec2(boxDimensions.x, -boxDimensions.y);
    vec2 boxUv = (fragCoord - boxTopLeft) / (boxBottomRight - boxTopLeft);

    FragColor = drawChar(font_charData[charIndex], boxUv, boxDimensions, TextFontScale,
                         false, false, TextTransparentBg) * int(wasCharIndexValid);
}
