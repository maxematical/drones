#version 430 core
in vec2 uv;
layout (std430, binding = 0) buffer myBuffer
{
    int characterCoordsLut[128];
    int characterOffsetLut[128];
    int charData[];
};

out vec4 FragColor;

uniform sampler2D theTexture;

void main()
{
    vec2 fontTextureDimensions = vec2(256.0, 128.0);
    int fontLineHeight = 14;

    FragColor = texture(theTexture, vec2(uv.x, -uv.y));
    int stringLength = charData[0];

    //if (mod(gl_FragCoord.x + 40.0*floor(gl_FragCoord.y/40.0), 40.0) < 20)
    //    FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    int charIndex = int(floor(gl_FragCoord.x / 32) + (256/32) * floor((256 - gl_FragCoord.y) / 32)) + 1;
    if (charIndex - 1 < stringLength)
        //FragColor = vec4(charData[charIndex] / 127.0, 0.0, 0.0, 1.0);
    {
        int char = charData[charIndex];

        vec2 squareTopLeft = vec2(floor(gl_FragCoord.x / 32) * 32,
            floor(gl_FragCoord.y / 32) * 32);
        vec2 squareBottomRight = squareTopLeft + vec2(32, 32);

        vec2 squareUv = (gl_FragCoord.xy - squareTopLeft) / (squareBottomRight - squareTopLeft);
        squareUv.y = 1.0 - squareUv.y;

        vec2 characterTopLeft = vec2(characterCoordsLut[char] >> 24,
            (characterCoordsLut[char] >> 16) & 255) / fontTextureDimensions;
        vec2 characterWidthHeight = vec2((characterCoordsLut[char] >> 8) & 255,
            (characterCoordsLut[char]) & 255) / fontTextureDimensions;
        vec2 characterOffset = vec2((characterOffsetLut[char] >> 8) & 255,
            characterOffsetLut[char] & 255) / fontTextureDimensions;

        vec2 characterUv = (characterTopLeft - characterOffset) + squareUv * (characterWidthHeight + characterOffset);
        bool isOutsideOffsetArea = characterUv.x >= characterTopLeft.x || characterUv.y >= characterTopLeft.y;

        FragColor = texture(theTexture, characterUv) * int(isOutsideOffsetArea);
    }
    else
        FragColor = vec4(0.0, 0.0, charIndex / 64.0, 1.0);

    // Enable this to see areas with partial transparency
    //if (FragColor.a == 0.0) FragColor = vec4(0.0, 0.0, 1.0, 1.0);
    //else if (FragColor.a == 1.0) FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
