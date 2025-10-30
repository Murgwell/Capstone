#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec2 u_playerPos;
uniform vec2 u_tilePos;
uniform float u_fadeRadius;
uniform float u_minAlpha;

void main() {
    vec4 color = texture2D(u_texture, v_texCoords);

    float dist = distance(u_playerPos, u_tilePos);
    float alpha = 1.0;

    if (dist < u_fadeRadius) {
        float fade = smoothstep(u_fadeRadius, 0.0, dist);
        alpha = mix(1.0, u_minAlpha, fade);
    }

    color.a *= alpha;

    gl_FragColor = color;
}
