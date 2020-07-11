#version 430 core

in vec2 uv;

uniform vec2 UiDimensionsPx;
uniform float BoxBorderSize;
uniform int BoxBorderColor;

out vec4 FragColor;

void main()
{
    vec2 distanceFromEdge = vec2(min(uv.x, 1.0 - uv.x),
                                 min(uv.y, 1.0 - uv.y));
    vec2 distanceFromEdgePx = distanceFromEdge * UiDimensionsPx;
    bool drawBorder = distanceFromEdgePx.x <= BoxBorderSize || distanceFromEdgePx.y <= BoxBorderSize;

    float colorR = ((BoxBorderColor >> 16) & 255) / 255.0;
    float colorG = ((BoxBorderColor >> 8) & 255) / 255.0;
    float colorB = (BoxBorderColor & 255) / 255.0;
    FragColor = mix(vec4(0.0), vec4(colorR, colorG, colorB, 1.0), int(drawBorder));
}
