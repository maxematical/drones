#version 430 core

out vec4 FragColor;

in vec2 uv;

void main()
{
    //FragColor = vec4(1.0, 0.0, 0.0, 1.0);
    FragColor = vec4(uv, 0.0, 1.0);
}
