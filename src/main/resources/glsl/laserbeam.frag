#version 430 core

in vec2 vertexUv;

uniform vec2 LaserDimensions;
uniform float Time;

out vec4 FragColor;

void main()
{
    // Horizontal "lines" moving backwards along the laser beam
    float light1 = 1.0 + 0.1 * cos((vertexUv.x + Time * 0.28) * LaserDimensions.x * 60);

    // Make the beam less bright at its sides
    float light2 = 1.0 - abs(2 * vertexUv.y - 1.0);

    // Add subtle, random-looking flicker to the beam
    float flicker = 0.5 + 0.5 * cos(Time * 300);

    // Add extra flicker at beginning of laser
    float t2 = Time - sqrt(1.0/6);
    flicker += 5 * clamp(1.0 - 6 * t2 * t2, 0.0, 1.0);

    // Taper at the end of the laser
    float distFromSides = abs(vertexUv.y - 0.5);
    float taper1Position = 8;
    float taper1Amount = vertexUv.x - (1.0 - 1.0 / taper1Position);
    float taper1 = 1.0 - (taper1Position * taper1Position * taper1Amount * taper1Amount +
        2 * distFromSides * distFromSides);
    taper1 = mix(taper1, 1.0 - 2 * distFromSides * distFromSides, int(taper1Amount < 0));
    taper1 += distFromSides;

    // Taper at the beginning of the laser
    float taper2Amount = (1.0 - vertexUv.x * 20) * 1.0;
    float taper2 = 1.0 - taper2Amount;
    taper2 = mix(taper2, 1.0, int(taper2Amount < 0));

    FragColor = (light1 * 0.1 + 0.9) * light2 * (flicker * 0.04 + 0.96) * taper1 * taper2 * vec4(0.85, 0.85, 1.8, 1.0);
}
