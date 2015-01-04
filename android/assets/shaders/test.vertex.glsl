attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
attribute vec4 a_color;

uniform mat4 u_worldTrans;
uniform mat4 u_projViewTrans;

varying vec3 v_position;
varying vec3 v_normal;
varying vec2 v_texCoord0;

void main() {
    v_position = vec3(u_worldTrans * a_position);
    v_normal = vec3(u_worldTrans * vec4(a_normal, 0.0));
    v_texCoord0 = a_texCoord0;

    gl_Position = u_projViewTrans * u_worldTrans * a_position;
}