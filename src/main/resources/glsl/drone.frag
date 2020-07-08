#version 430 core
out vec4 FragColor;

in vec2 vertexUv;

uniform int packedCharacterUv;
uniform vec2 bitmapDimensions;
uniform sampler2D bitmap;
uniform int droneColor;
uniform int ledColor;

void main()
{
    vec2 characterUvTopLeft = vec2((packedCharacterUv >> 23) & 511, (packedCharacterUv >> 14) & 511) / bitmapDimensions;
    vec2 characterUvWidthHeight = vec2((packedCharacterUv >> 7) & 63, packedCharacterUv & 63) / bitmapDimensions;

    vec2 bitmapUv = characterUvTopLeft + vertexUv * characterUvWidthHeight;

    FragColor = texture(bitmap, bitmapUv);

    float droneColorR = droneColor >> 16;
    float droneColorG = (droneColor >> 8) & 255;
    float droneColorB = droneColor & 255;
    FragColor.rgb *= vec3(droneColorR, droneColorG, droneColorB) / 255.0;

    // Draw dot (LED) at center of drone
    float dotRadius = 0.15;
    bool isCenter = length(vertexUv * 2.0 - 1.0) < dotRadius;

    vec3 dotRgb = vec3((ledColor >> 16) & 255,
        (ledColor >> 8) & 255,
        ledColor & 255) / vec3(255.0);
    FragColor = mix(FragColor, vec4(dotRgb, 1.0), int(isCenter));
}
