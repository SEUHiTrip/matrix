precision highp float;
varying highp vec2 textureCoordinate;
uniform sampler2D cursorFrame;
void main(void) {
	gl_FragColor = (texture2D(cursorFrame, textureCoordinate));
}
