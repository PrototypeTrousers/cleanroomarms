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

void main(){

    vec4 color = vec4(col.rgb, 1) * texture2D(texture, texCoord) * texture2D(lightmap, lightCoord);
    // ambient
    vec3 ambient = vec3(0.4);
    // diffuse
    vec3 normal = normalize(fragNorm);

    // Compute diffuse factor using step to avoid branching
    vec3 lightDir = normalize(lightPos0 - fragPos);
    float lightVisible = step(0.0, lightPos0.y); // 1.0 if lightPos0.y > 0, otherwise 0.0
    float diff = max(dot(normal, lightDir), 0.0) * lightVisible ;
    vec3 diffuse = diff * vec3(0.6, 0.6, 0.6);

    FragColor = vec4((diffuse + ambient) * color.rgb, color.a);
}