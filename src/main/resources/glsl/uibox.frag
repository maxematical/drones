#version 430 core

in vec2 uv;

uniform vec2 ElementDimensions;
uniform int BoxBorderSize;
uniform int BoxBorderColor;
uniform int BoxBackgroundColor;

out vec4 FragColor;

void main()
{
    vec2 distanceFromEdge = vec2(min(uv.x, 1.0 - uv.x),
                                 min(uv.y, 1.0 - uv.y));
    vec2 distanceFromEdgePx = distanceFromEdge * ElementDimensions;
    bool drawBorder = distanceFromEdgePx.x <= BoxBorderSize || distanceFromEdgePx.y <= BoxBorderSize;

    float borderR = ((BoxBorderColor >> 16) & 255) / 255.0;
    float borderG = ((BoxBorderColor >>  8) & 255) / 255.0;
    float borderB = ( BoxBorderColor        & 255) / 255.0;
    vec4 borderCol = vec4(borderR, borderG, borderB, 1.0);

    float backgroundR = ((BoxBackgroundColor >> 16) & 255) / 255.0;
    float backgroundG = ((BoxBackgroundColor >>  8) & 255) / 255.0;
    float backgroundB = ( BoxBackgroundColor        & 255) / 255.0;
    vec4 backgroundCol = vec4(backgroundR, backgroundG, backgroundB, 1.0);

    FragColor = mix(backgroundCol, borderCol, int(drawBorder));
}
