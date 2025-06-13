#version 330 compatibility

in vec2 texCoord;
in vec2 lightCoord;
in vec3 fragNorm;
in vec3 fragPos;
in vec3 lightPos0;
in vec3 lightPos1;
in vec4 col;

out vec4 FragColor;

uniform sampler2D texture;
uniform sampler2D lightmap;
uniform mat4 sunRotation;

void main() {
    // 1. Get the base color from Minecraft's textures and baked lightmap
    vec4 baseColor = col * texture2D(texture, texCoord) * texture2D(lightmap, lightCoord);

    // 3. Prepare the surface normal
    vec3 normal = normalize(fragNorm);
    vec3 totalLight = vec3(0.4);

    float diffuseFactor = max(dot(normal, vec3(-0.2, 1.0, -0.7)), 0.0);
    totalLight += vec3(0.6) * diffuseFactor;
    float diffuseFactor2 = max(dot(normal, vec3(0.2, 1.0, 0.7)), 0.0);
    totalLight += vec3(0.6) * diffuseFactor2;
    totalLight = min(totalLight, 1);

    FragColor = vec4(totalLight * baseColor.rgb, baseColor.a);
}