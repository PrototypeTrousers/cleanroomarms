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

    // --- Dynamic Lighting Calculation ---

    // 2. Define a base ambient light
    vec3 ambient = vec3(0.35); // A subtle ambient light

    // 3. Prepare the surface normal
    vec3 normal = normalize(fragNorm);

    // 4. Define a single, uniform white light color for all directions
    // Using a value less than 1.0 (e.g., 0.6) prevents the scene
    // from becoming too bright or "washed out" when multiple lights hit a surface.
    vec3 lightColor = vec3(0.6);

    // 5. Define the 6 light directions
    vec3 lightDirUp      = vec3(0.0, 1.0, 0.0);
    vec3 lightDirDown    = vec3(0.0, -1.0, 0.0);
    vec3 lightDirRight   = vec3(1.0, 0.0, 0.0);
    vec3 lightDirLeft    = vec3(-1.0, 0.0, 0.0);
    vec3 lightDirForward = vec3(0.0, 0.0, 1.0);
    vec3 lightDirBack    = vec3(0.0, 0.0, -1.0);

    // 6. Accumulate the diffuse light from all 6 sources
    vec3 totalDiffuse = vec3(0.0);

    // Calculate diffuse for each light and add it to the total.
    // Note that each calculation now uses the same 'lightColor' variable.
    totalDiffuse += max(dot(normal, lightDirUp), 0.0)      * lightColor;
    totalDiffuse += max(dot(normal, lightDirDown), 0.0)    * lightColor;
    totalDiffuse += max(dot(normal, lightDirRight), 0.0)   * lightColor;
    totalDiffuse += max(dot(normal, lightDirLeft), 0.0)    * lightColor;
    totalDiffuse += max(dot(normal, lightDirForward), 0.0) * lightColor;
    totalDiffuse += max(dot(normal, lightDirBack), 0.0)   * lightColor;

    // 7. Combine everything for the final color
    vec3 finalColor = (ambient + totalDiffuse) * baseColor.rgb;
    FragColor = vec4(finalColor, baseColor.a);
}