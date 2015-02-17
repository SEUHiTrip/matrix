precision highp float;
varying highp vec2 textureCoordinate;
uniform sampler2D cursorFrame;
uniform float rightEdge;
uniform float topEdge;
void main(void) {
	gl_FragColor = (texture2D(cursorFrame, textureCoordinate));
}
