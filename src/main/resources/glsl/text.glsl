layout (std430, binding = 0) buffer myBuffer
{
    int font_characterCoordsLut[128];
    int font_characterOffsetLut[128];
    vec2 font_bitmapDimensions;
    int font_colorTheme[16];
    int font_nChars;
    int font_charData[];
};

uniform sampler2D BitmapTexture;

vec4 drawChar(int, vec2, vec2, float);
vec4 drawChar(int, vec2, vec2, float, bool);
vec4 drawChar(int, vec2, vec2, float, bool, bool);
vec4 drawChar(int, vec2, vec2, float, bool, bool, bool);

vec4 drawChar(int charInfo, vec2 boxUv, vec2 boxDimensions, float fontScale) {
    return drawChar(charInfo, boxUv, boxDimensions, fontScale, false);
}

vec4 drawChar(int charInfo, vec2 boxUv, vec2 boxDimensions, float fontScale, bool flipY) {
    return drawChar(charInfo, boxUv, boxDimensions, fontScale, flipY, false);
}

vec4 drawChar(int charInfo, vec2 boxUv, vec2 boxDimensions, float fontScale, bool flipY, bool centerChar) {
    return drawChar(charInfo, boxUv, boxDimensions, fontScale, flipY, centerChar, false);
}

// Draws the character within a box.
// A monospaced string would be composed of equally-sized, spaced boxes.
//
// Arguments:
// 1) charInfo: the character and associated info, e.g. font_charData[someIndex]
// 2) boxUv: the uv coordinate within this box, ranging from (0,0) to (1,1)
// 3) boxDimensions: the dimensions, in pixels, of each box
// 4) fontScale: how much to scale the text from the bitmap
// 5) flipY: whether boxUv's y-coordinate needs to be flipped
// 6) centerChar: whether to ignore character offset and center the character in the box (99% of the time, false)
// 7) transparentBg: whether the character background should be transparent instead of a solid color
vec4 drawChar(int charInfo, vec2 boxUv, vec2 boxDimensions, float fontScale,
        bool flipY, bool centerChar, bool transparentBg) {
    // Determine the properties of the current character
    int char = charInfo & 255;
    int charFgColor = font_colorTheme[(charInfo >> 16) & 255];
    int charBgColor = font_colorTheme[(charInfo >> 8) & 255];
    int packedCoords = font_characterCoordsLut[char];
    int packedOffset = font_characterOffsetLut[char];

    // Determine the character's coordinates on the bitmap
    vec2 bitmapUvTopLeft = vec2((packedCoords >> 23) & 511, (packedCoords >> 14) & 511) / font_bitmapDimensions;
    vec2 bitmapUvDimensions = vec2((packedCoords >> 7) & 127, packedCoords & 127) / font_bitmapDimensions;

    vec2 bitmapOffset = vec2((packedOffset >> 8) & 255, packedOffset & 255) / font_bitmapDimensions;
    if (((packedOffset >> 17) & 1) == 1) bitmapOffset.x *= -1;
    if (((packedOffset >> 16) & 1) == 1) bitmapOffset.y *= -1;

    // Ignore offset if characters are to be centered
    bitmapOffset *= int(!centerChar);

    // Calculate character dimensions when it is actually scaled up
    vec2 charDimensions = fontScale * font_bitmapDimensions * (bitmapUvDimensions + bitmapOffset);

    // Flip boxUv.y if requested
    boxUv.y = mix(boxUv.y, 1.0 - boxUv.y, int(flipY));

    // Adjust the BoxUV to account for character dimensions
    // Two different algorithms to calculate adjustedUv, depending on whether we want to center the character or not
    vec2 boxPixel = boxUv * boxDimensions;
    vec2 adjustedUv1 = boxPixel / charDimensions;
    vec2 adjustedUv2 = (boxPixel - 0.5 * (boxDimensions - charDimensions)) / charDimensions;
    vec2 adjustedUv = mix(adjustedUv1, adjustedUv2, int(centerChar));
    bool outsideAdjustedUv = adjustedUv.x < 0 || adjustedUv.y < 0 || adjustedUv.x > 1 || adjustedUv.y > 1;

    // Calculate the UV of the glyph in the bitmap texture
    vec2 bitmapUv = (bitmapUvTopLeft - bitmapOffset) + adjustedUv * (bitmapUvDimensions + bitmapOffset);
    bool isPixelInOffset = bitmapUv.x < bitmapUvTopLeft.x || bitmapUv.y < bitmapUvTopLeft.y;

    // Sample the bitmap texture with the calculated UV. Exclude pixels in the padding and offset areas
    vec4 result = texture(BitmapTexture, bitmapUv) * int(!outsideAdjustedUv) * int(!isPixelInOffset);

    // Apply color theme
    float fgR = float((charFgColor >> 16) & 255) / 255.0;
    float fgG = float((charFgColor >>  8) & 255) / 255.0;
    float fgB = float( charFgColor        & 255) / 255.0;
    float fgA = 1.0;
    float bgR = float((charBgColor >> 16) & 255) / 255.0;
    float bgG = float((charBgColor >>  8) & 255) / 255.0;
    float bgB = float( charBgColor        & 255) / 255.0;
    float bgA = float(!transparentBg);

    result.rgb = mix(vec3(bgR, bgG, bgB), vec3(fgR, fgG, fgB), result.a);
    result.a = mix(bgA, fgA, result.a);

    return result;
}
