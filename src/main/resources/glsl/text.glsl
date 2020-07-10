layout (std430, binding = 0) buffer myBuffer
{
    int font_characterCoordsLut[128];
    int font_characterOffsetLut[128];
    vec2 font_bitmapDimensions;
    int font_colorTheme[16];
    int font_nChars;
    int font_charData[];
};

void drawChar(int charInfo) {
    // Determine the properties of the current character
    int char = charInfo & 255;
    int charFgColor = font_colorTheme[(charInfo >> 16) & 255];
    int charBgColor = font_colorTheme[(charInfo >> 8) & 255];
    int packedCoords = font_characterCoordsLut[char];
    int packedOffset = font_characterOffsetLut[char];

    // Determine the character's coordinates on the bitmap
    vec2 bitmapUvTopLeft = vec2((packedCoords >> 23) & 511, (packedCoords >> 14) & 511) / font_bitmapDimensions;
    vec2 bitmapUvWidthHeight = vec2((packedCoords >> 7) & 127, packedCoords & 127) / font_bitmapDimensions;

    vec2 bitmapOffset = vec2((packedOffset >> 8) & 255, packedOffset & 255) / font_bitmapDimensions;
    if (((packedOffset >> 17) & 1) == 1) bitmapOffset.x *= -1;
    if (((packedOffset >> 16) & 1) == 1) bitmapOffset.y *= -1;

    vec2 charSizePx = bitmapUvWidthHeight * font_bitmapDimensions * FontScale;

    // Calculate the corners of the current box
    vec2 boxWidthHeight = vec2(scaledSpacing, UiDimensionsPx.y);
    vec2 boxTopLeft = uiTopLeft + vec2(charIndex * scaledSpacing + extraSpace, 0);
    vec2 boxBottomRight = boxTopLeft + boxWidthHeight;

    //FragColor = vec4((fragCoord - boxTopLeft) / (boxBottomRight - boxTopLeft), 0.0, 1.0);return;

    // Apply padding in the box based on the character's dimensions
    vec2 glyphTopLeft = boxTopLeft + (boxWidthHeight - charSizePx) / 2;
    vec2 glyphBottomRight = glyphTopLeft + charSizePx;
    vec2 boxPaddedPosition = (fragCoord - glyphTopLeft) / (glyphBottomRight - glyphTopLeft);

    bool isPixelInGlyph = fragCoord.x >= glyphTopLeft.x && fragCoord.y >= glyphTopLeft.y &&
    fragCoord.x <= glyphBottomRight.x && fragCoord.y <= glyphBottomRight.y; // only include thse pixels

    // Determine the coordinates from the bitmap to use
    vec2 boxUv = boxPaddedPosition;
    boxUv.y = 1.0 - boxUv.y;

    vec2 bitmapUv = (bitmapUvTopLeft - bitmapOffset) + boxUv * (bitmapUvWidthHeight + bitmapOffset);

    bool isPixelInOffset = bitmapUv.x < bitmapUvTopLeft.x || bitmapUv.y < bitmapUvTopLeft.y;

    // Sample the bitmap texture with the calculated UV. Exclude pixels in the padding and offset areas
    FragColor = texture(BitmapTexture, bitmapUv) * int(isPixelInGlyph) * int(!isPixelInOffset);

    // Apply color theme
    float fgR = float((charFgColor >> 16) & 255) / 255.0;
    float fgG = float((charFgColor >>  8) & 255) / 255.0;
    float fgB = float( charFgColor        & 255) / 255.0;
    float bgR = float((charBgColor >> 16) & 255) / 255.0;
    float bgG = float((charBgColor >>  8) & 255) / 255.0;
    float bgB = float( charBgColor        & 255) / 255.0;

    FragColor.rgb = mix(vec3(bgR, bgG, bgB), vec3(fgR, fgG, fgB), FragColor.a);
    FragColor.a = int(!wasCharIndexInvalid);
}
