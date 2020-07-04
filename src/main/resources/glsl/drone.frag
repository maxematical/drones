#version 430 core
out vec4 FragColor;

in vec2 vertexUv;

uniform int packedCharacterUv;
uniform vec2 bitmapDimensions;
uniform sampler2D bitmap;

void main()
{
    vec2 characterUvTopLeft = vec2((packedCharacterUv >> 23) & 511, (packedCharacterUv >> 14) & 511) / bitmapDimensions;
    vec2 characterUvWidthHeight = vec2((packedCharacterUv >> 7) & 63, packedCharacterUv & 63) / bitmapDimensions;

    vec2 bitmapUv = characterUvTopLeft + vertexUv * characterUvWidthHeight;

    FragColor = texture(bitmap, bitmapUv);
}
