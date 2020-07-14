#version 430 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aUv;

out vec2 uv; // y-up UV of the quad

uniform mat4 QuadMatrix;
uniform vec2 ScreenDimensions;
uniform vec2 ElementDimensions;
uniform vec2 ElementPosition;

//uniform vec2 WindowSize;
//uniform vec2 UiAnchorPoint;
//uniform vec2 UiPositionPx;
//uniform vec2 UiDimensionsPx;

void main()
{
    gl_Position = QuadMatrix * vec4(aPos, 1.0);
    uv = aUv;

//    vec2 pos = aPos.xy;
//    pos -= UiAnchorPoint;
//    pos *= UiDimensionsPx / WindowSize;
//    pos -= 1.0;
//    pos += 2 * UiPositionPx / WindowSize;
//
//    gl_Position = vec4(pos.xy, 0.0, 1.0);
//    uv = aPos.xy * 0.5 + 0.5;
}
