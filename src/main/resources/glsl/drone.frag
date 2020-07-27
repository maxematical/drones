#version 430 core
out vec4 FragColor;

in vec2 vertexUv;

uniform vec2 TextureDimensions;
uniform sampler2D Texture;
uniform int DroneColor;
uniform int LedColor;
uniform bool IsSelected;

void main()
{
    vec2 rotatedUv = vertexUv.yx;
    rotatedUv.y = 1.0 - rotatedUv.y;

    float largerDimension = max(TextureDimensions.x, TextureDimensions.y);
    vec2 textureScale = largerDimension / TextureDimensions;
    vec2 uv = rotatedUv * textureScale + (1 - textureScale) * 0.5;
    bool uvValid = (uv == clamp(uv, 0.0, 1.0));
    FragColor = texture(Texture, uv) * int(uvValid);

    float droneColorR = DroneColor >> 16;
    float droneColorG = (DroneColor >> 8) & 255;
    float droneColorB = DroneColor & 255;
    float textureAlpha = FragColor.a;
    FragColor.rgb *= vec3(droneColorR, droneColorG, droneColorB) / 255.0;

    // Draw selection outline
    float outlineScale = 0.5;
    int range = 2;
    bool drawOutline = false;
    for (int y = 0; y < range; y++) {
        for (int x = 0; x < range; x++) {
            vec2 texUv = uv + vec2(x - range / 2, y - range / 2) * outlineScale / TextureDimensions;
            bool texUvValid = (texUv == clamp(texUv, 0.0, 1.0));

            float sampledAlpha = texture(Texture, texUv).a * int(texUvValid);
            bool differentAlpha = abs(sampledAlpha - textureAlpha) > 0.1;
            drawOutline = drawOutline || differentAlpha;
        }
    }
    drawOutline = drawOutline && IsSelected;
    FragColor = mix(FragColor, vec4(1.0, 0.0, 0.0, 1.0), int(drawOutline));

    // Draw dot (LED) at center of drone
    float dotRadius = 0.17;
    float dotAA = 0.05;
    float dotGlow = 5;
    float distanceFromQuadCenter = length(vertexUv * 2.0 - 1.0);
    float circleness = ((dotRadius - distanceFromQuadCenter) / dotAA * 0.5) + 0.5;
    circleness = clamp(circleness, 0, 1);

    float glow = 0.5 * clamp(1.0 - distanceFromQuadCenter * dotGlow, 0, 1);

    vec3 dotRgb = vec3((LedColor >> 16) & 255,
        (LedColor >> 8) & 255,
        LedColor & 255) / vec3(255.0);
    FragColor = mix(FragColor, vec4(dotRgb, 1.0), circleness);
    FragColor = mix(FragColor, vec4(1.0), glow);
}
