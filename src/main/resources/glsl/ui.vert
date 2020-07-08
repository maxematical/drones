#version 430 core
layout (location = 0) in vec3 aPos;

uniform vec2 WindowSize;
uniform vec2 UiAnchorPoint;
uniform vec2 UiPositionPx;
uniform vec2 UiDimensionsPx;

void main()
{
    vec2 pos = aPos.xy;
    pos -= UiAnchorPoint;
    pos *= UiDimensionsPx / WindowSize;
    pos -= 1.0;
    pos += 2 * UiPositionPx / WindowSize;

    gl_Position = vec4(pos.xy, 0.0, 1.0);
}
