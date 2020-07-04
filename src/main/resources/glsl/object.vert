#version 430 core
layout (location = 0) in vec3 aPos;

uniform vec2 cameraPos;

void main()
{
    gl_Position = vec4(aPos * 0.3, 1.0);
}
