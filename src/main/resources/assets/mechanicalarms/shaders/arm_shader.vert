#version 330 compatibility

layout (location = 0) in vec3 in_pos;
layout (location = 1) in vec2 in_texcoord;
layout (location = 2) in vec3 in_normal;
layout (location = 3) in vec2 in_light;
layout (location = 4) in mat4 in_transform;
layout (location = 8) in vec4 in_color;


out vec2 texCoord;
out vec2 lightCoord;
out vec3 fragNorm;
out vec3 fragPos;
out vec3 lightPos0;
out vec3 lightPos1;
out vec4 col;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 sunRotation;

void main() {
    gl_Position = projection * view * in_transform * vec4(in_pos, 1);

    fragPos = vec3(in_transform * vec4(in_pos, 1.0));
    fragNorm = mat3(transpose(inverse(in_transform))) * in_normal;
    lightPos0 = (vec4(0, 100, 0, 1) * sunRotation).xyz;

    //0 and 1 are used for the p and q coordinates because p defaults to 0 and q defaults to 1
    texCoord = (gl_TextureMatrix[0] * vec4(in_texcoord, 0, 1)).st;
    float skyLightCoord = in_light.x * (1.0 / 16.0) + 1/32.0;
    float blockLightCoord = in_light.y * (1.0 / 16.0) + 1/32.0;

    lightCoord = vec2(blockLightCoord, skyLightCoord);

    col = in_color;
}