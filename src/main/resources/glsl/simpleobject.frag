#version 430 core

out vec4 FragColor;

in vec2 vertexUv;

uniform sampler2D BitmapTexture;

uniform int PackedCharacterUv;
uniform vec2 BitmapDimensions;

uniform float NumberPatterns;
uniform bool SwitchPatterns;

void main() {
    vec2 uv2 = vertexUv * NumberPatterns - floor(vertexUv * NumberPatterns);

    bool switchPattern = SwitchPatterns &&
        mod(floor(vertexUv.x * NumberPatterns) + floor(vertexUv.y * NumberPatterns), 2.0) == 1.0;
    uv2 = mix(uv2, uv2.yx, float(switchPattern));

    vec2 characterUvTopLeft = vec2((PackedCharacterUv >> 23) & 511, (PackedCharacterUv >> 14) & 511) / BitmapDimensions;
    vec2 characterUvWidthHeight = vec2((PackedCharacterUv >> 7) & 63, PackedCharacterUv & 63) / BitmapDimensions;

    vec2 bitmapUv = characterUvTopLeft + uv2 * characterUvWidthHeight;

    FragColor = texture(BitmapTexture, bitmapUv);
}
