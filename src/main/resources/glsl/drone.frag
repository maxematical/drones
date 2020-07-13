#version 430 core
out vec4 FragColor;

in vec2 vertexUv;

uniform int packedCharacterUv;
uniform vec2 bitmapDimensions;
uniform sampler2D bitmap;
uniform int droneColor;
uniform int ledColor;
uniform bool isSelected;

void main()
{
    vec2 characterUvTopLeft = vec2((packedCharacterUv >> 23) & 511, (packedCharacterUv >> 14) & 511) / bitmapDimensions;
    vec2 characterUvWidthHeight = vec2((packedCharacterUv >> 7) & 63, packedCharacterUv & 63) / bitmapDimensions;

    vec2 bitmapUv = characterUvTopLeft + vertexUv * characterUvWidthHeight;

    FragColor = texture(bitmap, bitmapUv);
    float textureAlpha = FragColor.a;

    float droneColorR = droneColor >> 16;
    float droneColorG = (droneColor >> 8) & 255;
    float droneColorB = droneColor & 255;
    FragColor.rgb *= vec3(droneColorR, droneColorG, droneColorB) / 255.0;

    // Draw selection outline
    float outlineScale = 0.1;
    int range = 3;
    bool drawOutline = false;
    for (int y = 0; y < range; y++) {
        for (int x = 0; x < range; x++) {
            vec2 texUv = bitmapUv + vec2(x - range / 2, y - range / 2) * outlineScale / bitmapDimensions;
            bool differentAlpha = abs(texture(bitmap, texUv).a - textureAlpha) > 0.1;
            drawOutline = drawOutline || differentAlpha;
        }
    }
    drawOutline = drawOutline && isSelected;
    FragColor = mix(FragColor, vec4(1.0, 0.0, 0.0, 1.0), int(drawOutline));

    // Draw dot (LED) at center of drone
    float dotRadius = 0.17;
    float dotAA = 0.05;
    float dotGlow = 5;
    float distanceFromQuadCenter = length(vertexUv * 2.0 - 1.0);
    float circleness = ((dotRadius - distanceFromQuadCenter) / dotAA * 0.5) + 0.5;
    circleness = clamp(circleness, 0, 1);

    float glow = 0.5 * clamp(1.0 - distanceFromQuadCenter * dotGlow, 0, 1);

    vec3 dotRgb = vec3((ledColor >> 16) & 255,
        (ledColor >> 8) & 255,
        ledColor & 255) / vec3(255.0);
    FragColor = mix(FragColor, vec4(dotRgb, 1.0), circleness);
    FragColor = mix(FragColor, vec4(1.0), glow);
}
