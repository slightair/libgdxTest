#ifdef GL_ES
precision mediump float;
#endif

uniform vec3 u_lightPos;
uniform vec4 u_color;
uniform sampler2D u_texture;

varying vec3 v_position;
varying vec3 v_normal;
varying vec2 v_texCoord0;

void main() {
    float distance = length(u_lightPos - v_position);
    vec3 lightVector = normalize(u_lightPos - v_position);
    float diffuse = max(dot(v_normal, lightVector), 0.1);

    diffuse = diffuse * (1.0 / (1.0 + 0.00025 * (distance * distance)));

    gl_FragColor = u_color * texture2D(u_texture, v_texCoord0) * diffuse;
}