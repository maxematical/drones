#version 430 core
#include "text.glsl"

uniform vec2 WindowSize;

uniform vec2 UiAnchorPoint;
uniform vec2 UiPositionPx;
uniform vec2 UiDimensionsPx;

uniform float FontScale;
uniform float FontSpacing;
uniform int FontAlign;
uniform bool FontTransparentBg;

out vec4 FragColor;

void main()
{
    float scaledSpacing = FontSpacing * FontScale;
    vec2 fragCoord = gl_FragCoord.xy;

    // Figure out the index of which character we're looking at
    vec2 uiTopLeft = UiPositionPx - UiDimensionsPx * (UiAnchorPoint * 0.5 + 0.5);
    vec2 relativePixel = fragCoord - uiTopLeft;
    float widthChars = font_nChars * scaledSpacing; // the width of all boxes, in pixels

    // Calculate extra space on the left side of the quad due to text alignment
    // If the text is right-aligned or center-aligned, there will be extra space there
    float extraSpace = UiDimensionsPx.x - widthChars;   // if the text is right-aligned, this is the extra space at the
                                                        // left of the quad, where there aren't any characters

    float mulLut[3]; // multiply extraSpace by a number depending on whether it is left, right, or center aligned
    mulLut[0] = 0.0; // left-aligned: extraSpace should be zero
    mulLut[1] = 1.0; // right-aligned: extraSpace should be same
    mulLut[2] = 0.5; // center-aligned: extraSpace should be half
    extraSpace *= mulLut[FontAlign];

    // Calculate the index of the character in this box
    int charIndex = int(floor(( relativePixel.x - extraSpace ) / scaledSpacing));

    // Avoid out-of-range array accesses by setting charIndex to 0 if it is out-of-bounds
    bool wasCharIndexValid = charIndex >= 0 && charIndex < font_nChars;
    charIndex *= int(wasCharIndexValid);

    vec2 boxWidthHeight = vec2(scaledSpacing, UiDimensionsPx.y);
    vec2 boxTopLeft = uiTopLeft + vec2(charIndex * scaledSpacing + extraSpace, 0);
    vec2 boxBottomRight = boxTopLeft + boxWidthHeight;
    vec2 boxUv = (fragCoord - boxTopLeft) / (boxBottomRight - boxTopLeft);

    FragColor = drawChar(font_charData[charIndex], boxUv, boxWidthHeight,
                         FontScale, true, false, FontTransparentBg) * int(wasCharIndexValid);
}
