#version 430 core
layout (std430, binding = 0) buffer myBuffer
{
    int characterCoordsLut[128];
    int characterOffsetLut[128];
    vec2 fontTextureDimensions;
    int colorTheme[16];
    int nChars;
    int charData[];
};

out vec4 FragColor;

uniform sampler2D theTexture;
uniform vec2 WindowSize;
uniform float TileSize;

vec4 drawCharacter(vec2 pixel, int charIndex);
vec4 drawEmptyTile(vec2 pixel, int charIndex);

void main()
{
    float lineHeight = 14;

    vec2 pixel = gl_FragCoord.xy;
    pixel.y = WindowSize.y - pixel.y;

    int charIndex = int(floor(pixel.x / TileSize) + (WindowSize.x / TileSize) * floor(pixel.y / TileSize));
    bool isCharacterHere = charIndex < nChars;
    FragColor = mix(drawEmptyTile(pixel, charIndex), drawCharacter(pixel, charIndex), bool(isCharacterHere));

    // Enable this to see areas with partial transparency
    //if (FragColor.a == 0.0) FragColor = vec4(0.0, 0.0, 1.0, 1.0);
    //else if (FragColor.a == 1.0) FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}

vec4 drawCharacter(vec2 pixel, int charIndex) {
    // Get info about the current character
    int charInfo = charData[charIndex];
    int char = charInfo & 255;
    int bgColorIndex = (charInfo >> 8) & 255;
    int fgColorIndex = (charInfo >> 16) & 255;

    // Determine the UV within this current square (tile) -- (0,0) means top left of this square, (1,1) means bottom
    // right
    vec2 squareTopLeft = vec2(floor(pixel.x / TileSize) * TileSize,
    floor(pixel.y / TileSize) * TileSize);
    vec2 squareBottomRight = squareTopLeft + vec2(TileSize);

    vec2 squareUv = (pixel.xy - squareTopLeft) / (squareBottomRight - squareTopLeft);

    // Read info about this character for the current bitmap from lookup tables
    int packedCoords = characterCoordsLut[char];
    vec2 characterTopLeft = vec2(packedCoords >> 24, (packedCoords >> 16) & 255) / fontTextureDimensions;
    vec2 characterWidthHeight = vec2((packedCoords >> 8) & 255, packedCoords & 255) / fontTextureDimensions;

    int packedOffset = characterOffsetLut[char];
    vec2 characterOffset = vec2((packedOffset >> 8) & 255, packedOffset & 255) / fontTextureDimensions;

    if ((characterOffsetLut[char] >> 17 & 1) == 1)
    characterOffset.x *= -1;
    if ((characterOffsetLut[char] >> 16 & 1) == 1)
    characterOffset.y *= -1;

    // Calculate an offsetted square UV, which slightly shrinks the character and centers it within the tile
    float charWidth = (characterWidthHeight.x + characterOffset.x) * fontTextureDimensions.x * 3;
    float charHeight = (characterWidthHeight.y + characterOffset.y) * fontTextureDimensions.y * 3;
    // I have no idea how these calculations make sense, but it works
    vec2 squareUv2 = vec2(TileSize / charWidth, TileSize / charHeight) *
    (squareUv - vec2((TileSize - charWidth) / 2 / TileSize, (TileSize - charHeight) / 2 / TileSize));

    // Calculate the UV of the character on the bitmap
    vec2 characterUv = (characterTopLeft - characterOffset) + squareUv2 * (characterWidthHeight + characterOffset);

    // Sample the bitmap texture
    FragColor = texture(theTexture, characterUv);

    // Hide areas not meant to be shown (this is hard to explain)
    bool isInOffsetArea = characterUv.x < characterTopLeft.x && characterUv.y < characterTopLeft.y;
    bool inRelevantArea = clamp(squareUv2, 0.0, 1.0) == squareUv2;
    FragColor *= int(!isInOffsetArea) * int(inRelevantArea);

    // Apply the appropriate foreground/background color
    int color = int(mix(colorTheme[bgColorIndex], colorTheme[fgColorIndex], FragColor.a));

    // The color from colorTheme is a hex color, e.g. 0xFF0000 for red -- we have to unpack this into a vec4
    FragColor = vec4(((color >> 16) & 255) / 255.0,
    ((color >> 8) & 255) / 255.0,
    (color & 255) / 255.0,
    1.0);

    // Draw grid
    bool shouldDrawGrid = mod(pixel.x + 1, TileSize) <= 2.0 || mod(pixel.y + 1, TileSize) <= 2.0;
    FragColor = mix(FragColor, vec4(0.0, 0.0, 1.0, 1.0), int(shouldDrawGrid));

    return FragColor;
}

vec4 drawEmptyTile(vec2 pixel, int charIndex) {
    float totalNumTiles = (WindowSize.x * WindowSize.y) / (TileSize * TileSize);
    FragColor = vec4(0.0, 0.0, charIndex / totalNumTiles, 1.0);

    return FragColor;
}
