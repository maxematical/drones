#version 430 core

out vec4 FragColor;

in vec2 vertexUv;

uniform sampler2D BitmapTexture;

uniform int PackedCharacterUv;
uniform vec2 BitmapDimensions;

void main() {
    float nPatterns = 2.0;
    vec2 uv2 = vertexUv * nPatterns - floor(vertexUv * nPatterns);

    bool switchPattern = mod(floor(vertexUv.x * nPatterns) + floor(vertexUv.y * nPatterns), 2.0) == 1.0;
    uv2 = mix(uv2, uv2.yx, float(switchPattern));

    vec2 characterUvTopLeft = vec2((PackedCharacterUv >> 23) & 511, (PackedCharacterUv >> 14) & 511) / BitmapDimensions;
    vec2 characterUvWidthHeight = vec2((PackedCharacterUv >> 7) & 63, PackedCharacterUv & 63) / BitmapDimensions;

    vec2 bitmapUv = characterUvTopLeft + uv2 * characterUvWidthHeight;

    FragColor = texture(BitmapTexture, bitmapUv);
}
