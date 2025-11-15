#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
varying vec4 v_color;

uniform sampler2D u_texture;
uniform vec2 u_playerPos;
uniform float u_fadeRadius;
uniform float u_minAlpha;

void main() {
    vec4 texColor = texture2D(u_texture, v_texCoords);
    vec2 pixelPos = gl_FragCoord.xy;

    float dist = distance(pixelPos, u_playerPos);
    float alpha = 1.0;

    if (dist < u_fadeRadius) {
        float t = dist / u_fadeRadius;
        // Smoothstep gives a soft falloff curve
        alpha = mix(u_minAlpha, 1.0, smoothstep(0.0, 1.0, t));
    }

    gl_FragColor = vec4(texColor.rgb, texColor.a * alpha) * v_color;
}
