#version 430 core

uniform vec2 DebugPosition;
uniform float DebugRadius;

out vec4 FragColor;

void main()
{
    vec2 pixel = gl_FragCoord.xy;
    vec2 delta = DebugPosition - pixel;
    bool drawDot = dot(delta, delta) <= (DebugRadius * DebugRadius);

    FragColor = mix(vec4(0.0), vec4(1.0, 0.0, 0.0, 1.0), float(drawDot));
}
