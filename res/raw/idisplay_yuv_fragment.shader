precision highp float;

uniform sampler2D videoFrame;
uniform sampler2D videoFrame2;
uniform sampler2D videoFrame3;
uniform float rightEdge;
uniform float topEdge;

varying highp vec2 textureCoordinate;

void main(void) {
	if (textureCoordinate.x < 0.0 || textureCoordinate.x > rightEdge || textureCoordinate.y < 0.0 || textureCoordinate.y > topEdge) {
		gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
	} else {
		highp vec3 yuv;
		highp vec3 rgb;
		highp float nx, ny;
		nx = (textureCoordinate.x);
		ny = (textureCoordinate.y);
		yuv.x = texture2D(videoFrame, textureCoordinate).r;
		yuv.y = texture2D(videoFrame2, vec2(nx, ny)).r - 0.5;
		yuv.z = texture2D(videoFrame3, vec2(nx, ny)).r - 0.5;
		rgb = mat3(1, 1, 1, 0, -.34414, 1.772, 1.402, -.71414, 0) * yuv;
		gl_FragColor = vec4(rgb, 1);
	}
}
