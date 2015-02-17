precision highp float;

uniform sampler2D u_videoFrame;
uniform sampler2D u_videoFrame2;
uniform sampler2D u_videoFrame3;
uniform float u_rightEdge;
uniform float u_topEdge;

varying vec2 v_TexCoordinate;

void main(void) {
	if (v_TexCoordinate.x < -0.5 || v_TexCoordinate.x > u_rightEdge || v_TexCoordinate.y < -0.5 || v_TexCoordinate.y > u_topEdge) {
		gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
	} else {
		highp vec3 yuv;
		highp vec3 rgb;
		highp float nx, ny;
		nx = (v_TexCoordinate.x);
		ny = (v_TexCoordinate.y);
		yuv.x = texture2D(u_videoFrame, v_TexCoordinate).r;
		yuv.y = texture2D(u_videoFrame2, vec2(nx, ny)).r - 0.5;
		yuv.z = texture2D(u_videoFrame3, vec2(nx, ny)).r - 0.5;
		rgb = mat3(1, 1, 1, 0, -.34414, 1.772, 1.402, -.71414, 0) * yuv;
		gl_FragColor = vec4(rgb, 1);
	}
}
