uniform mat4 u_Model;
uniform mat4 u_MVMatrix;
uniform mat4 u_MVPMatrix;
uniform vec3 u_LightPos;

attribute vec4 a_Position;
attribute vec4 a_Color;
attribute vec4 a_Normal;

varying vec4 v_Color;
varying vec4 v_Normal;

void main() {
    v_Color = a_Color;
    v_Normal = a_Normal;

   gl_Position =  u_MVMatrix * a_Position;

}