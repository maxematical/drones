#version 430 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aUv;

uniform vec2 cameraPos;
uniform mat4 cameraMatrix;
uniform mat4 modelMatrix;

out vec2 vertexUv;

void main()
{
    gl_Position = cameraMatrix * modelMatrix * vec4(aPos, 1.0);
    vertexUv = aUv;
}
