#version 430 core
layout (location = 0) in vec3 aPos;

uniform vec2 cameraPos;
uniform mat4 cameraMatrix;
uniform mat4 modelMatrix;

void main()
{
    gl_Position = cameraMatrix * modelMatrix * vec4(aPos, 1.0);
}
