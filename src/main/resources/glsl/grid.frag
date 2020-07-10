#version 430 core
#include "text.glsl"

out vec4 FragColor;

uniform sampler2D theTexture;
uniform vec2 WindowSize;
uniform float TileSize;
uniform vec2 CameraPos;
uniform vec2 GridTopLeft;
uniform float GridWidth;

vec4 drawCharacterTile(vec2 pixel, int charIndex);
vec4 drawEmptyTile(vec2 pixel, int charIndex);

void main()
{
    float lineHeight = 14;

    vec2 pixel = gl_FragCoord.xy;
    pixel.y = WindowSize.y - pixel.y;
    pixel -= WindowSize * 0.5;
    pixel.x += (CameraPos.x - GridTopLeft.x) * TileSize;
    pixel.y -= (CameraPos.y + GridTopLeft.y) * TileSize;

    float tileX = floor(pixel.x / TileSize);
    float tileY = floor(pixel.y / TileSize);

    int charIndex = int(mix(font_nChars, tileX + GridWidth * tileY, int(tileX < GridWidth)));
    bool isCharacterHere = (charIndex >= 0 && charIndex < font_nChars) && tileX >= 0;
    charIndex *= int(isCharacterHere); // avoid out-of-bounds array accesses
    FragColor = mix(drawEmptyTile(pixel, charIndex), drawCharacterTile(pixel, charIndex), int(isCharacterHere));

    // Enable this to see areas with partial transparency
    //if (FragColor.a == 0.0) FragColor = vec4(0.0, 0.0, 1.0, 1.0);
    //else if (FragColor.a == 1.0) FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}

vec4 drawCharacterTile(vec2 pixel, int charIndex) {
    // Determine the UV within this current tile
    vec2 squareTopLeft = vec2(floor(pixel.x / TileSize) * TileSize, floor(pixel.y / TileSize) * TileSize);
    vec2 squareBottomRight = squareTopLeft + vec2(TileSize);
    vec2 squareUv = (pixel.xy - squareTopLeft) / (squareBottomRight - squareTopLeft);

    // Draw the character in this tile
    vec4 result = drawChar(font_charData[charIndex], squareUv, vec2(TileSize), 3.0, false, true);

    // Draw grid
    bool shouldDrawGrid = mod(pixel.x + 1, TileSize) <= 2.0 || mod(pixel.y + 1, TileSize) <= 2.0;
    result = mix(result, vec4(0.0, 0.0, 1.0, 1.0), int(shouldDrawGrid));

    return result;
}

vec4 drawEmptyTile(vec2 pixel, int charIndex) {
    return vec4(0.0, 0.0, 0.33, 1.0);
}
