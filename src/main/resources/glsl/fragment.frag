#version 430 core
in vec2 uv;
layout (std430, binding = 0) buffer myBuffer
{
    int characterCoordsLut[128];
    int characterOffsetLut[128];
    vec2 fontTextureDimensions;
    int nChars;
    int charData[];
};

out vec4 FragColor;

uniform sampler2D theTexture;
uniform vec2 WindowSize;
uniform float TileSize;

void main()
{
    float lineHeight = 14;

    FragColor = texture(theTexture, vec2(uv.x, -uv.y));

    //if (mod(gl_FragCoord.x + 40.0*floor(gl_FragCoord.y/40.0), 40.0) < 20)
    //    FragColor = vec4(0.0, 0.0, 0.0, 1.0);

    vec2 pixel = gl_FragCoord.xy;
    pixel.y = WindowSize.y - pixel.y;

    int charIndex = int(floor(pixel.x / TileSize) + (WindowSize.x / TileSize) * floor(pixel.y / TileSize));
    if (charIndex < nChars)
    {
        int char = charData[charIndex];

        vec2 squareTopLeft = vec2(floor(pixel.x / TileSize) * TileSize,
            floor(pixel.y / TileSize) * TileSize);
        vec2 squareBottomRight = squareTopLeft + vec2(TileSize);

        vec2 squareUv = (pixel.xy - squareTopLeft) / (squareBottomRight - squareTopLeft);

        vec2 characterTopLeft = vec2(characterCoordsLut[char] >> 24,
            (characterCoordsLut[char] >> 16) & 255) / fontTextureDimensions;
        vec2 characterWidthHeight = vec2((characterCoordsLut[char] >> 8) & 255, (characterCoordsLut[char]) & 255) /
            fontTextureDimensions;
        vec2 characterOffset = vec2((characterOffsetLut[char] >> 8) & 255, characterOffsetLut[char] & 255) /
            fontTextureDimensions;

        if ((characterOffsetLut[char] >> 17 & 1) == 1)
            characterOffset.x *= -1;
        if ((characterOffsetLut[char] >> 16 & 1) == 1)
            characterOffset.y *= -1;

        float charWidth = (characterWidthHeight.x + characterOffset.x) * fontTextureDimensions.x * 3;
        float charHeight = (characterWidthHeight.y + characterOffset.y) * fontTextureDimensions.y * 3;
        // I have no idea how these calculations make sense, but it works
        vec2 squareUv2 = vec2(TileSize / charWidth, TileSize / charHeight) *
            (squareUv - vec2((TileSize - charWidth) / 2 / TileSize, (TileSize - charHeight) / 2 / TileSize));



        vec2 characterUv = (characterTopLeft - characterOffset) + squareUv2 * (characterWidthHeight + characterOffset);
        bool isOutsideOffsetArea = characterUv.x >= characterTopLeft.x || characterUv.y >= characterTopLeft.y;

        FragColor = texture(theTexture, characterUv) * int(isOutsideOffsetArea);

        FragColor = (clamp(squareUv2, 0.0, 1.0) == squareUv2) ? FragColor : vec4(1.0, 0.0, 0.0, 0.0);
        if (mod(pixel.x + 1, TileSize) <= 2.0 || mod(pixel.y + 1, TileSize) <= 2.0)
            FragColor = vec4(0.0, 0.0, 1.0, 1.0);
    }
    else
        FragColor = vec4(0.0, 0.0, charIndex / (WindowSize.x / TileSize * WindowSize.y / TileSize), 1.0);

    // Enable this to see areas with partial transparency
    //if (FragColor.a == 0.0) FragColor = vec4(0.0, 0.0, 1.0, 1.0);
    //else if (FragColor.a == 1.0) FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
