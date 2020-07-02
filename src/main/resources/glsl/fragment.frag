#version 430 core
in vec2 uv;
layout (std430, binding = 0) buffer myBuffer
{
    int charData[1];
};

out vec4 FragColor;

uniform sampler2D theTexture;

void main()
{
    //FragColor = vec4(uv, 0.0, 1.0) * 0.5 + texture(theTexture, uv) * 0.5;
    FragColor = texture(theTexture, vec2(uv.x, -uv.y));
    //FragColor = vec4(data[0], 0.0, 0.0, 1.0);
    int x = charData[0];
    FragColor = vec4(0.0, 0.0, x / 127.0, 1.0);

    // Enable this to see areas with partial transparency
    //if (FragColor.a == 0.0) FragColor = vec4(0.0, 0.0, 1.0, 1.0);
    //else if (FragColor.a == 1.0) FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
