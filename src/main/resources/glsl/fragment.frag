#version 330 core
in vec2 uv;
out vec4 FragColor;

uniform sampler2D theTexture;

void main()
{
    //FragColor = vec4(uv, 0.0, 1.0) * 0.5 + texture(theTexture, uv) * 0.5;
    FragColor = texture(theTexture, vec2(uv.x, -uv.y));

    // Enable this to see areas with partial transparency
    //if (FragColor.a == 0.0) FragColor = vec4(0.0, 0.0, 1.0, 1.0);
    //else if (FragColor.a == 1.0) FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
