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

void main(){

    vec4 color = vec4(col.rgb, 1) * texture2D(texture, texCoord) * texture2D(lightmap, lightCoord);
    // ambient
    vec3 ambient = vec3(0.2, 0.2, 0.2);
    // diffuse
    vec3 normal = normalize(fragNorm);

    float diff = max(dot(normal, normalize(lightPos0 - fragPos)), 0.0);
    diff += max(dot(normal, normalize(lightPos1 - fragPos)), 0.0f);
    vec3 diffuse = diff * vec3(0.6, 0.6, 0.6);

    FragColor = vec4((ambient + diffuse) * color.rgb, color.a);
}